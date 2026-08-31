package com.shop.changhyeon.promotion.service;

import com.shop.changhyeon.promotion.constant.*;
import com.shop.changhyeon.promotion.dto.*;
import com.shop.changhyeon.promotion.entity.*;
import com.shop.changhyeon.promotion.repository.*;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionOptionRepository optionRepository;
    private final PromotionEntryRepository entryRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> published(PromotionCategory category, String email) {
        Member member = email == null ? null : memberRepository.findByEmail(email);
        return promotionRepository.findPublished(LocalDate.now(), category).stream()
                .map(p -> PromotionResponse.from(p, member != null && entryRepository.existsByPromotion_IdAndMember_Id(p.getId(), member.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PromotionResponse detail(Long id, String email, boolean admin) {
        Promotion p = find(id);
        LocalDate today = LocalDate.now();
        if (!admin && (!p.isActive() || today.isBefore(p.getStartDate()) || today.isAfter(p.getEndDate()))) throw new IllegalArgumentException("현재 조회할 수 없는 프로모션입니다.");
        Member member = email == null ? null : memberRepository.findByEmail(email);
        return PromotionResponse.from(p, member != null && entryRepository.existsByPromotion_IdAndMember_Id(id, member.getId()));
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> adminList() { return promotionRepository.findAll().stream().sorted(Comparator.comparing(Promotion::getId).reversed()).map(p -> PromotionResponse.from(p, false)).toList(); }

    public PromotionResponse create(PromotionRequest request) { Promotion p = new Promotion(); apply(p, request); return PromotionResponse.from(promotionRepository.save(p), false); }
    public PromotionResponse update(Long id, PromotionRequest request) {
        Promotion p = find(id);
        if (entryRepository.countByPromotion_Id(id) > 0) {
            List<String> currentOptions = p.getOptions().stream().map(PromotionOption::getLabel).toList();
            List<String> requestedOptions = request.options() == null ? List.of() : request.options().stream()
                    .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
            if (request.type() != PromotionType.LOTTERY || !currentOptions.equals(requestedOptions)) {
                throw new IllegalStateException("응모 내역이 있는 프로모션의 유형과 선택 항목은 변경할 수 없습니다.");
            }
        }
        apply(p, request);
        return PromotionResponse.from(p, false);
    }
    public void delete(Long id) { promotionRepository.delete(find(id)); }

    public PromotionEntryResponse enter(Long promotionId, Long optionId, String email) {
        Promotion p = find(promotionId);
        if (p.getType() != PromotionType.LOTTERY) throw new IllegalArgumentException("추첨 프로모션만 응모할 수 있습니다.");
        LocalDate today = LocalDate.now();
        if (!p.isActive() || today.isBefore(p.getStartDate()) || today.isAfter(p.getEndDate())) throw new IllegalArgumentException("응모 기간이 아닙니다.");
        Member member = memberRepository.findByEmail(email);
        if (member == null) throw new IllegalArgumentException("회원 정보를 찾을 수 없습니다.");
        if (entryRepository.existsByPromotion_IdAndMember_Id(promotionId, member.getId())) throw new IllegalStateException("이미 응모한 프로모션입니다.");
        PromotionOption option = optionRepository.findById(optionId).filter(o -> o.getPromotion().getId().equals(promotionId)).orElseThrow(() -> new IllegalArgumentException("선택 항목이 올바르지 않습니다."));
        PromotionEntry saved = entryRepository.saveAndFlush(new PromotionEntry(p, member, option));
        return new PromotionEntryResponse(saved.getId(), promotionId, optionId, option.getLabel(), saved.getCreatedAt());
    }

    private Promotion find(Long id) { return promotionRepository.findDetail(id).orElseThrow(() -> new IllegalArgumentException("프로모션을 찾을 수 없습니다.")); }
    private void apply(Promotion p, PromotionRequest r) {
        if (r.endDate().isBefore(r.startDate())) throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        if (r.category() == PromotionCategory.REGIONAL && (r.regionName() == null || r.regionName().isBlank())) throw new IllegalArgumentException("지역별 프로모션은 지역명을 입력해야 합니다.");
        List<String> options = r.options() == null ? List.of() : r.options().stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
        if (r.type() == PromotionType.LOTTERY && options.size() < 2) throw new IllegalArgumentException("추첨 프로모션은 선택 항목을 2개 이상 입력해야 합니다.");
        p.setCategory(r.category()); p.setType(r.type()); p.setRegionName(r.category() == PromotionCategory.NATIONAL ? null : r.regionName().trim()); p.setTitle(r.title().trim()); p.setSummary(r.summary().trim()); p.setContent(r.content().trim()); p.setImageUrl(r.imageUrl() == null ? null : r.imageUrl().trim()); p.setStartDate(r.startDate()); p.setEndDate(r.endDate()); p.setActive(r.active()); p.replaceOptions(r.type() == PromotionType.LOTTERY ? options : List.of());
    }
}
