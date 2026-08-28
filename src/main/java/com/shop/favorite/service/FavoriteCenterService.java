package com.shop.favorite.service;

import com.shop.center.entity.Center;
import com.shop.center.repository.CenterRepository;
import com.shop.favorite.dto.FavoriteCenterResponseDto;
import com.shop.favorite.entity.FavoriteCenter;
import com.shop.favorite.repository.FavoriteCenterRepository;
import com.shop.member.entity.Member;
import com.shop.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteCenterService {

    private final FavoriteCenterRepository favoriteCenterRepository;
    private final MemberRepository memberRepository;
    private final CenterRepository centerRepository;


    @Transactional(readOnly = true)
    public List<FavoriteCenterResponseDto> getMyFavorites(
            String email
    ) {
        Member member = getMember(email);

        return favoriteCenterRepository
                .findByMember_IdOrderByIdDesc(
                        member.getId()
                )
                .stream()
                .map(FavoriteCenterResponseDto::from)
                .toList();
    }


    public FavoriteCenterResponseDto addFavorite(
            String email,
            Long centerId
    ) {
        Member member = getMember(email);

        Center center = centerRepository
                .findById(centerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "헌혈센터를 찾을 수 없습니다."
                        )
                );

        return favoriteCenterRepository
                .findByMember_IdAndCenter_Id(
                        member.getId(),
                        centerId
                )
                .map(FavoriteCenterResponseDto::from)
                .orElseGet(() -> {
                    FavoriteCenter favorite =
                            new FavoriteCenter(
                                    member,
                                    center
                            );

                    return FavoriteCenterResponseDto.from(
                            favoriteCenterRepository.save(
                                    favorite
                            )
                    );
                });
    }


    public void removeFavorite(
            String email,
            Long centerId
    ) {
        Member member = getMember(email);

        favoriteCenterRepository
                .findByMember_IdAndCenter_Id(
                        member.getId(),
                        centerId
                )
                .ifPresent(
                        favoriteCenterRepository::delete
                );
    }


    private Member getMember(String email) {
        Member member =
                memberRepository.findByEmail(email);

        if (member == null) {
            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }

        return member;
    }
}