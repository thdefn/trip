package com.trip.diary.service;

import com.trip.diary.domain.model.*;
import com.trip.diary.domain.repository.*;
import com.trip.diary.domain.type.ParticipantType;
import com.trip.diary.dto.CreatePostForm;
import com.trip.diary.dto.PostDetailDto;
import com.trip.diary.dto.UpdatePostForm;
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.LocationException;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import com.trip.diary.util.ImageManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private LocationRepository locationRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private PostLikeRedisRepository postLikeRedisRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private ImageManager imageManager;

    @InjectMocks
    private PostService postService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

    Member participant1 = Member.builder()
            .id(2L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();
    Member participant2 = Member.builder()
            .id(3L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .title("임의의 타이틀")
            .isPrivate(true)
            .description("임의의 설명")
            .leader(member)
            .participants(List.of(
                    Participant.builder()
                            .member(member)
                            .type(ParticipantType.ACCEPTED)
                            .build(),
                    Participant.builder()
                            .member(participant1)
                            .type(ParticipantType.ACCEPTED)
                            .build(),
                    Participant.builder()
                            .member(participant2)
                            .type(ParticipantType.ACCEPTED)
                            .build()
            ))
            .build();

    List<MultipartFile> images = List.of(
            new MockMultipartFile("images", "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes()),
            new MockMultipartFile("images", "image.jpg",
                    MediaType.IMAGE_JPEG_VALUE, "abcde".getBytes())
    );

    @Test
    @DisplayName("여행 기록 생성 성공")
    void createTest_success() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도 도착입니당")
                .location("제주공항")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("제주공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(locationRepository.findFirstByTripOrderByIdDesc(any()))
                .willReturn(Optional.of(location));
        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));
        //when
        PostDetailDto result = postService.create(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Collection<PostImage>> postImageCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(postImageRepository, times(1)).saveAll(postImageCaptor.capture());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals(2, result.getImagePaths().size());
        assertEquals("제주도 도착입니당", postCaptor.getValue().getContent());
        assertEquals("제주공항", postCaptor.getValue().getLocation().getName());
        assertEquals(2, postImageCaptor.getValue().size());
        assertEquals(1L, postImageCaptor.getValue().iterator().next().getPost().getId());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());
    }

    @Test
    @DisplayName("여행 기록 생성 성공 - 이전 로케이션이 없을때")
    void createTest_successWhenOptionalLocationIsEmpty() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도로 출발입니당")
                .location("김포공항")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(locationRepository.findFirstByTripOrderByIdDesc(any()))
                .willReturn(Optional.empty());
        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));
        //when
        postService.create(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals("김포공항", postCaptor.getValue().getLocation().getName());
        assertEquals("김포공항", locationCaptor.getValue().getName());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());
    }

    @Test
    @DisplayName("여행 기록 생성 성공 - 직전 로케이션의 이름이 현재 위치와 다를 때")
    void createTest_successWhenPreviousLocationNameIsDifferent() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("진짜맛있다")
                .location("아베베 베이커리")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(locationRepository.findFirstByTripOrderByIdDesc(any()))
                .willReturn(Optional.of(location));
        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());
        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));
        //when
        postService.create(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals("아베베 베이커리", postCaptor.getValue().getLocation().getName());
        assertEquals("아베베 베이커리", locationCaptor.getValue().getName());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());
    }

    @Test
    @DisplayName("여행 기록 생성 실패 - 해당 여행 기록장 없음")
    void createTest_failWhenNotFoundTrip() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도 도착입니당")
                .location("제주공항")
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.empty());
        //when
        TripException exception = assertThrows(TripException.class, () -> postService.create(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 생성 실패 - 유저가 여행 기록장에 대한 참여자가 아님")
    void createTest_failWhenNotAuthorityWriteTrip() {
        //given
        CreatePostForm form = CreatePostForm.builder()
                .content("제주도 도착입니당")
                .location("제주공항")
                .build();

        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .leader(participant1)
                .participants(List.of(
                        Participant.builder()
                                .member(participant1)
                                .type(ParticipantType.ACCEPTED)
                                .build(),
                        Participant.builder()
                                .member(participant2)
                                .type(ParticipantType.ACCEPTED)
                                .build()
                ))
                .build();

        given(tripRepository.findById(anyLong())).willReturn(Optional.of(trip));
        //when
        TripException exception = assertThrows(TripException.class, () -> postService.create(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_WRITE_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 수정 성공")
    void updateTest_success() {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("제주도 도착입니당")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .location(location)
                        .member(member)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build()));

        given(postLikeRedisRepository.countByPostId(anyLong()))
                .willReturn(1L);

        given(imageManager.uploadImages(any(), anyString()))
                .willReturn(List.of("/post/1.jpg",
                        "/post/2.jpg"));

        given(postRepository.save(any()))
                .willReturn(Post.builder()
                        .id(1L)
                        .content("제주도 도착입니당")
                        .location(location)
                        .member(member)
                        .build());

        given(postLikeRedisRepository.existsByPostIdAndUserId(anyLong(), anyLong()))
                .willReturn(true);
        //when
        PostDetailDto result = postService.update(1L, form, images, member);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        ArgumentCaptor<Collection<PostImage>> postImageCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Location> locationCaptor = ArgumentCaptor.forClass(Location.class);
        //then
        verify(postRepository, times(1)).save(postCaptor.capture());
        verify(postImageRepository, times(1)).saveAll(postImageCaptor.capture());
        verify(postImageRepository, times(1)).deleteAllInBatch(any());
        verify(locationRepository, times(1)).save(locationCaptor.capture());
        assertEquals(2, result.getImagePaths().size());
        assertTrue(result.getIsReaderLiked());
        assertEquals("제주도 도착입니당", postCaptor.getValue().getContent());
        assertEquals("김포공항", postCaptor.getValue().getLocation().getName());
        assertEquals(2, postImageCaptor.getValue().size());
        assertEquals(1L, postImageCaptor.getValue().iterator().next().getPost().getId());
        assertEquals("/post/1.jpg", locationCaptor.getValue().getThumbnailPath());

    }

    @Test
    @DisplayName("여행 기록 수정 실패 - 해당 기록 없음")
    void updateTest_failWhenNotFoundPost() {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("제주도 도착입니당")
                .build();

        given(postRepository.findById(any())).willReturn(Optional.empty());
        //when
        PostException exception = assertThrows(PostException.class, () -> postService.update(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 수정 실패 - 유저가 기록의 작성자가 아님")
    void updateTest_failWhenNotPostOwner() {
        //given
        UpdatePostForm form = UpdatePostForm.builder()
                .content("제주도 도착입니당")
                .build();

        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .location(location)
                        .member(participant1)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build()));
        //when
        PostException exception = assertThrows(PostException.class, () -> postService.update(1L, form, images, member));
        //then
        assertEquals(ErrorCode.NOT_POST_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 삭제 성공")
    void deleteTest_success() {
        //given
        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .posts(List.of(Post.builder()
                                .id(1L)
                                .build(),
                        Post.builder()
                                .id(2L)
                                .build()
                ))
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();
        Post post = Post.builder()
                .id(1L)
                .content("김포공항 도착입니당")
                .location(location)
                .member(member)
                .images(List.of(
                        PostImage.builder()
                                .id(1L)
                                .imagePath("/post/uuid202030103030.jpg")
                                .build(),
                        PostImage.builder()
                                .id(2L)
                                .imagePath("/post/uuid202030103031.jpg")
                                .build()
                ))
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(post));
        //when
        postService.delete(1L, member);
        //then
        verify(postRepository, times(1)).delete(any());
        verify(locationRepository, times(0)).delete(any());
    }

    @Test
    @DisplayName("여행 기록 삭제 성공 - 기록의 로케이션에 대한 post가 오직 하나일때")
    void deleteTest_successWhenLocationHaveOnlyPost() {
        //given
        Location location = Location.builder()
                .id(1L)
                .trip(trip)
                .name("김포공항")
                .posts(List.of(Post.builder()
                        .id(1L)
                        .build()))
                .thumbnailPath("/post/uuid202030103030.jpg")
                .build();
        Post post = Post.builder()
                .id(1L)
                .content("김포공항 도착입니당")
                .location(location)
                .member(member)
                .images(List.of(
                        PostImage.builder()
                                .id(1L)
                                .imagePath("/post/uuid202030103030.jpg")
                                .build(),
                        PostImage.builder()
                                .id(2L)
                                .imagePath("/post/uuid202030103031.jpg")
                                .build()
                ))
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(post));
        //when
        postService.delete(1L, member);
        //then
        verify(postRepository, times(1)).delete(any());
        verify(locationRepository, times(1)).delete(any());
    }

    @Test
    @DisplayName("여행 기록 삭제 성공 - 해당 기록 없음")
    void deleteTest_failWhenNotFoundPost() {
        //given
        given(postRepository.findById(any())).willReturn(Optional.empty());
        //when
        PostException exception = assertThrows(PostException.class, () -> postService.delete(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("여행 기록 삭제 성공 - 유저가 작성자가 아님")
    void deleteTest_failWhenNotPostOwner() {
        //given
        Post post = Post.builder()
                .id(1L)
                .content("김포공항 도착입니당")
                .member(participant1)
                .images(List.of(
                        PostImage.builder()
                                .id(1L)
                                .imagePath("/post/uuid202030103030.jpg")
                                .build(),
                        PostImage.builder()
                                .id(2L)
                                .imagePath("/post/uuid202030103031.jpg")
                                .build()
                ))
                .build();

        given(postRepository.findById(any())).willReturn(
                Optional.of(post));
        //when
        PostException exception = assertThrows(PostException.class, () -> postService.delete(1L, member));
        //then
        assertEquals(ErrorCode.NOT_POST_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("좋아요 성공 - 생성")
    void like_success() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .member(participant1)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build())
                );
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(postLikeRedisRepository.existsByPostIdAndUserId(anyLong(), anyLong()))
                .willReturn(false);
        //when
        postService.like(1L, member);
        //then
        verify(postLikeRedisRepository, times(1)).save(anyLong(), any());
    }

    @Test
    @DisplayName("좋아요 성공 - 취소")
    void like_successCancel() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .member(participant1)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build())
                );
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(postLikeRedisRepository.existsByPostIdAndUserId(anyLong(), anyLong()))
                .willReturn(true);
        //when
        postService.like(1L, member);
        //then
        verify(postLikeRedisRepository, times(1)).delete(anyLong(), any());
    }

    @Test
    @DisplayName("좋아요 실패 - 쓰기 권한 없음")
    void like_failWhenNotAuthorityWriteTrip() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(Post.builder()
                        .id(1L)
                        .content("김포공항 도착입니당")
                        .member(participant1)
                        .images(List.of(
                                PostImage.builder()
                                        .id(1L)
                                        .imagePath("/post/uuid202030103030.jpg")
                                        .build(),
                                PostImage.builder()
                                        .id(2L)
                                        .imagePath("/post/uuid202030103031.jpg")
                                        .build()
                        ))
                        .build())
                );
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class, () -> postService.like(1L, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_WRITE_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("로케이션 별 포스트 조회 성공")
    void readPostsByLocationTest_success() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .build();
        given(locationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Location.builder()
                                .trip(trip)
                                .id(1L)
                                .name("제주공항")
                                .thumbnailPath("/posts/1.jpg")
                                .posts(List.of(
                                        Post.builder()
                                                .id(1L)
                                                .content("제주공항에서 본 고양이 짱귀엽다")
                                                .location(Location.builder().id(1L).name("제주공항").build())
                                                .member(member)
                                                .images(
                                                        List.of(PostImage.builder()
                                                                .id(1L)
                                                                .imagePath("/posts/1.jpg")
                                                                .build()
                                                        )
                                                )
                                                .build(),
                                        Post.builder()
                                                .id(2L)
                                                .content("제주도 첫끼니는 버거킹..ㅋ")
                                                .member(participant1)
                                                .location(Location.builder().id(1L).name("제주공항").build())
                                                .images(
                                                        List.of(PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/2.jpg")
                                                                        .build(),
                                                                PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/3.jpg")
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                ))
                                .build()
                ));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(true);
        given(postLikeRedisRepository.countByPostId(1L)).willReturn(2L);
        given(postLikeRedisRepository.countByPostId(2L)).willReturn(0L);
        given(postLikeRedisRepository.existsByPostIdAndUserId(1L, 1L))
                .willReturn(true);
        given(postLikeRedisRepository.existsByPostIdAndUserId(2L, 1L))
                .willReturn(false);
        //when
        List<PostDetailDto> result = postService.readPostsByLocation(1L, member);
        //then
        assertEquals(2L, result.get(0).getLikeOfPosts());
        assertEquals(1L, result.get(0).getId());
        assertEquals("제주공항에서 본 고양이 짱귀엽다", result.get(0).getContent());
        assertTrue(result.get(0).getIsReader());
        assertNotEquals(member.getNickname(), result.get(0).getAuthorNickname());
        assertEquals(member.getId(), result.get(0).getId());
        assertTrue(result.get(0).getIsReaderLiked());
        assertEquals(0L, result.get(1).getLikeOfPosts());
        assertFalse(result.get(1).getIsReader());
        assertEquals(participant1.getNickname(), result.get(1).getAuthorNickname());
        assertEquals(participant1.getId(), result.get(1).getId());
        assertFalse(result.get(1).getIsReaderLiked());
    }

    @Test
    @DisplayName("로케이션 별 포스트 조회 실패 - 해당 로케이션 없음")
    void deleteTest_failWhenNotFoundLocation() {
        //given
        given(locationRepository.findById(any())).willReturn(Optional.empty());
        //when
        LocationException exception = assertThrows(LocationException.class,
                () -> postService.readPostsByLocation(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_LOCATION, exception.getErrorCode());
    }

    @Test
    @DisplayName("로케이션 별 포스트 조회 실패 - 여행 기록장에 대한 읽기 권한 없음")
    void deleteTest_failWhenNotAuthorityReadTrip() {
        //given
        Trip trip = Trip.builder()
                .id(1L)
                .title("임의의 타이틀")
                .isPrivate(true)
                .description("임의의 설명")
                .build();
        given(locationRepository.findById(anyLong()))
                .willReturn(Optional.of(
                        Location.builder()
                                .trip(trip)
                                .id(1L)
                                .name("제주공항")
                                .thumbnailPath("/posts/1.jpg")
                                .posts(List.of(
                                        Post.builder()
                                                .id(1L)
                                                .content("제주공항에서 본 고양이 짱귀엽다")
                                                .location(Location.builder().id(1L).name("제주공항").build())
                                                .member(member)
                                                .images(
                                                        List.of(PostImage.builder()
                                                                .id(1L)
                                                                .imagePath("/posts/1.jpg")
                                                                .build()
                                                        )
                                                )
                                                .build(),
                                        Post.builder()
                                                .id(2L)
                                                .content("제주도 첫끼니는 버거킹..ㅋ")
                                                .member(participant1)
                                                .location(Location.builder().id(1L).name("제주공항").build())
                                                .images(
                                                        List.of(PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/2.jpg")
                                                                        .build(),
                                                                PostImage.builder()
                                                                        .id(2L)
                                                                        .imagePath("/posts/3.jpg")
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                ))
                                .build()
                ));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any())).willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class,
                () -> postService.readPostsByLocation(1L, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_READ_TRIP, exception.getErrorCode());
    }

}