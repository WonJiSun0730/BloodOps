package com.shop.changhyeon.promotion.service;

import com.shop.changhyeon.donation.repository.DonationHistoryRepository;
import com.shop.changhyeon.promotion.constant.*;
import com.shop.changhyeon.promotion.dto.*;
import com.shop.changhyeon.promotion.entity.*;
import com.shop.changhyeon.promotion.repository.*;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {
    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final PromotionRepository promotionRepository;
    private final PromotionOptionRepository optionRepository;
    private final PromotionEntryRepository entryRepository;
    private final MemberRepository memberRepository;
    private final DonationHistoryRepository donationHistoryRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> published(PromotionCategory category, String email) {
        Member member = email == null ? null : memberRepository.findByEmail(email);
        return promotionRepository.findPublished(LocalDate.now(), category).stream()
                .map(promotion -> responseFor(promotion, member))
                .toList();
    }

    @Transactional(readOnly = true)
    public PromotionResponse detail(Long id, String email, boolean admin) {
        Promotion promotion = find(id);
        LocalDate today = LocalDate.now();
        if (!admin && (!promotion.isActive()
                || today.isBefore(promotion.getStartDate())
                || today.isAfter(promotion.getEndDate()))) {
            throw new IllegalArgumentException("현재 조회할 수 없는 프로모션입니다.");
        }
        Member member = email == null ? null : memberRepository.findByEmail(email);
        return responseFor(promotion, member);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> adminList() {
        return promotionRepository.findAll().stream()
                .sorted(Comparator.comparing(Promotion::getId).reversed())
                .map(promotion -> PromotionResponse.from(promotion, false))
                .toList();
    }

    public PromotionResponse create(PromotionRequest request) {
        Promotion promotion = new Promotion();
        apply(promotion, request, false);
        return PromotionResponse.from(promotionRepository.save(promotion), false);
    }

    public PromotionResponse update(Long id, PromotionRequest request) {
        Promotion promotion = find(id);
        boolean hasEntries = entryRepository.countByPromotion_Id(id) > 0;
        if (hasEntries) {
            List<String> currentOptions = promotion.getOptions().stream()
                    .map(PromotionOption::getLabel)
                    .toList();
            List<String> requestedOptions = normalizedOptions(request);
            if (request.type() != PromotionType.LOTTERY
                    || !currentOptions.equals(requestedOptions)) {
                throw new IllegalStateException(
                        "응모 내역이 있는 프로모션의 유형과 선택 항목은 변경할 수 없습니다."
                );
            }
        }
        apply(promotion, request, hasEntries);
        return PromotionResponse.from(promotion, false);
    }

    public PromotionResponse uploadImage(Long id, MultipartFile image) {
        validateImage(image);
        Promotion promotion = find(id);
        try {
            promotion.updateImage(
                    image.getBytes(),
                    image.getContentType(),
                    safeOriginalName(image.getOriginalFilename())
            );
        } catch (IOException exception) {
            throw new IllegalArgumentException("이미지 파일을 저장하지 못했습니다.");
        }
        return PromotionResponse.from(promotion, false);
    }

    @Transactional(readOnly = true)
    public PromotionImage getImage(Long id) {
        Promotion promotion = find(id);
        if (promotion.getImageData() == null || promotion.getImageData().length == 0) {
            throw new IllegalArgumentException("프로모션 이미지를 찾을 수 없습니다.");
        }
        return new PromotionImage(
                promotion.getImageData(),
                promotion.getImageContentType()
        );
    }

    public void delete(Long id) {
        promotionRepository.delete(find(id));
    }

    public PromotionEntryResponse enter(Long promotionId, Long optionId, String email) {
        Promotion promotion = find(promotionId);
        if (promotion.getType() != PromotionType.LOTTERY) {
            throw new IllegalArgumentException("추첨 프로모션만 응모할 수 있습니다.");
        }
        LocalDate today = LocalDate.now();
        if (!promotion.isActive()
                || today.isBefore(promotion.getStartDate())
                || today.isAfter(promotion.getEndDate())) {
            throw new IllegalArgumentException("응모 기간이 아닙니다.");
        }
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        }
        if (entryRepository.existsByPromotion_IdAndMember_Id(
                promotionId, member.getId()
        )) {
            throw new IllegalStateException("이미 응모한 프로모션입니다.");
        }
        if (!hasDonationDuringPromotion(member, promotion)) {
            throw new IllegalStateException(
                    "관리자가 설정한 헌혈 실적 인정 기간에 완료된 헌혈 기록이 있어야 응모할 수 있습니다."
            );
        }
        PromotionOption option = optionRepository.findById(optionId)
                .filter(item -> item.getPromotion().getId().equals(promotionId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "선택 항목이 올바르지 않습니다."
                ));
        PromotionEntry saved = entryRepository.saveAndFlush(
                new PromotionEntry(promotion, member, option)
        );
        return new PromotionEntryResponse(
                saved.getId(),
                promotionId,
                optionId,
                option.getLabel(),
                saved.getCreatedAt()
        );
    }

    private PromotionResponse responseFor(Promotion promotion, Member member) {
        boolean entered = member != null
                && entryRepository.existsByPromotion_IdAndMember_Id(
                promotion.getId(), member.getId()
        );
        boolean eligible = promotion.getType() != PromotionType.LOTTERY
                || (member != null && hasDonationDuringPromotion(member, promotion));
        String eligibilityMessage = promotion.getType() != PromotionType.LOTTERY
                ? null
                : member == null
                    ? "로그인 후 응모 자격을 확인할 수 있습니다."
                    : eligible
                        ? "헌혈 실적 인정 기간에 완료된 헌혈 기록이 확인되었습니다."
                        : "관리자가 설정한 헌혈 실적 인정 기간에 완료된 헌혈 기록이 있어야 응모할 수 있습니다.";
        return PromotionResponse.from(
                promotion, entered, eligible, eligibilityMessage
        );
    }

    private boolean hasDonationDuringPromotion(
            Member member,
            Promotion promotion
    ) {
        LocalDate eligibilityStartDate =
                promotion.getDonationEligibilityStartDate() == null
                        ? promotion.getStartDate()
                        : promotion.getDonationEligibilityStartDate();
        return donationHistoryRepository
                .existsByMember_IdAndDonationDateBetween(
                        member.getId(),
                        eligibilityStartDate,
                        promotion.getEndDate()
                );
    }

    private Promotion find(Long id) {
        return promotionRepository.findDetail(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "프로모션을 찾을 수 없습니다."
                ));
    }

    private void apply(
            Promotion promotion,
            PromotionRequest request,
            boolean preserveOptions
    ) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "종료일은 시작일보다 빠를 수 없습니다."
            );
        }
        LocalDate eligibilityStartDate =
                request.donationEligibilityStartDate() == null
                        ? request.startDate()
                        : request.donationEligibilityStartDate();
        if (eligibilityStartDate.isAfter(request.endDate())) {
            throw new IllegalArgumentException(
                    "헌혈 실적 인정 시작일은 프로모션 종료일보다 늦을 수 없습니다."
            );
        }

        String regionName = null;
        if (request.category() == PromotionCategory.REGIONAL) {
            if (request.regionName() == null || request.regionName().isBlank()) {
                throw new IllegalArgumentException(
                        "지역별 프로모션은 지역을 선택해야 합니다."
                );
            }
            regionName = PromotionRegion.from(request.regionName()).name();
        }

        List<String> options = normalizedOptions(request);
        if (request.type() == PromotionType.LOTTERY && options.isEmpty()) {
            throw new IllegalArgumentException(
                    "추첨 프로모션은 선택 항목을 1개 이상 입력해주세요."
            );
        }

        promotion.setCategory(request.category());
        promotion.setType(request.type());
        promotion.setRegionName(regionName);
        promotion.setTitle(request.title().trim());
        promotion.setSummary(normalizeNullableText(request.summary()));
        promotion.setContent(normalizeNullableText(request.content()));
        promotion.setStartDate(request.startDate());
        promotion.setEndDate(request.endDate());
        promotion.setDonationEligibilityStartDate(eligibilityStartDate);
        promotion.setActive(request.active());
        if (!preserveOptions) {
            promotion.replaceOptions(
                    request.type() == PromotionType.LOTTERY
                            ? options
                            : List.of()
            );
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private List<String> normalizedOptions(PromotionRequest request) {
        return request.options() == null
                ? List.of()
                : request.options().stream()
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .distinct()
                    .toList();
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("첨부할 이미지 파일을 선택해주세요.");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("이미지는 최대 5MB까지 첨부할 수 있습니다.");
        }
        if (!ALLOWED_IMAGE_TYPES.contains(image.getContentType())) {
            throw new IllegalArgumentException(
                    "JPG, PNG, WebP, GIF 이미지만 첨부할 수 있습니다."
            );
        }
    }

    private String safeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "promotion-image";
        }
        String normalizedName = originalName.replace("\\", "/");
        return normalizedName.substring(normalizedName.lastIndexOf('/') + 1);
    }

    public record PromotionImage(byte[] data, String contentType) {}
}
