package ru.practicum.ewm.main.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventRepository;
import ru.practicum.ewm.main.event.EventState;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRequestRepository eventRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public EventRequestDto createRequest(Long userId, Long eventId) {
        User user = findUserByIdOrThrow(userId);
        Event event = findEventByIdOrThrow(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя отправить заявку на свое событие!");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Нельзя отправить заявку на неопубликованное событие!");
        }

        if (eventRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Заявка на это событие уже существует!");
        }

        long confirmedRequests = getConfirmedRequests(eventId);
        if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников события!");
        }

        EventRequest eventRequest = new EventRequest();
        eventRequest.setCreated(LocalDateTime.now());
        eventRequest.setRequester(user);
        eventRequest.setEvent(event);

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            eventRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            eventRequest.setStatus(RequestStatus.PENDING);
        }

        EventRequest savedRequest = eventRequestRepository.save(eventRequest);
        return EventRequestMapper.toEventRequestDto(savedRequest);
    }

    @Override
    public List<EventRequestDto> findAllUserRequests(Long userId) {
        findUserByIdOrThrow(userId);

        return eventRequestRepository.findAllByRequesterId(userId).stream()
                .map(EventRequestMapper::toEventRequestDto)
                .toList();
    }

    @Override
    public EventRequestDto cancelRequest(Long userId, Long requestId) {
        findUserByIdOrThrow(userId);
        EventRequest eventRequest = findRequestByIdOrThrow(requestId);

        if (!eventRequest.getRequester().getId().equals(userId)) {
            throw new ConflictException("Можно отменить только свою заявку!");
        }

        eventRequest.setStatus(RequestStatus.CANCELED);
        EventRequest updatedRequest = eventRequestRepository.save(eventRequest);
        return EventRequestMapper.toEventRequestDto(updatedRequest);
    }

    @Override
    public List<EventRequestDto> findAllEventRequests(Long userId, Long eventId) {
        findUserByIdOrThrow(userId);
        Event event = findEventByIdOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Получить заявки может только инициатор события!");
        }

        return eventRequestRepository.findAllByEventId(eventId).stream()
                .map(EventRequestMapper::toEventRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResultDto updateRequestStatus(Long userId,
                                                                 Long eventId,
                                                                 EventRequestStatusUpdateDto updateDto) {
        findUserByIdOrThrow(userId);
        Event event = findEventByIdOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Изменить статус заявок может только инициатор события!");
        }

        List<EventRequest> requests = eventRequestRepository.findAllByEventIdAndIdIn(eventId, updateDto.getRequestIds());

        EventRequestStatusUpdateResultDto resultDto = new EventRequestStatusUpdateResultDto();

        if (updateDto.getStatus() == RequestStatus.REJECTED) {
            for (EventRequest eventRequest : requests) {
                if (eventRequest.getStatus() != RequestStatus.PENDING) {
                    throw new ConflictException("Можно изменять только заявки в статусе PENDING!");
                }

                eventRequest.setStatus(RequestStatus.REJECTED);
                EventRequest savedRequest = eventRequestRepository.save(eventRequest);
                resultDto.getRejectedRequests().add(EventRequestMapper.toEventRequestDto(savedRequest));
            }

            return resultDto;
        }

        if (updateDto.getStatus() == RequestStatus.CONFIRMED) {
            long confirmedRequests = getConfirmedRequests(eventId);

            if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит участников события!");
            }

            for (EventRequest eventRequest : requests) {
                if (eventRequest.getStatus() != RequestStatus.PENDING) {
                    throw new ConflictException("Можно изменять только заявки в статусе PENDING!");
                }

                if (event.getParticipantLimit() == 0 || confirmedRequests < event.getParticipantLimit()) {
                    eventRequest.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests++;
                    EventRequest savedRequest = eventRequestRepository.save(eventRequest);
                    resultDto.getConfirmedRequests().add(EventRequestMapper.toEventRequestDto(savedRequest));
                } else {
                    eventRequest.setStatus(RequestStatus.REJECTED);
                    EventRequest savedRequest = eventRequestRepository.save(eventRequest);
                    resultDto.getRejectedRequests().add(EventRequestMapper.toEventRequestDto(savedRequest));
                }
            }
        }

        return resultDto;
    }

    private User findUserByIdOrThrow(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException("Такого пользователя нет!");
        }
        return user.get();
    }

    private Event findEventByIdOrThrow(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new NotFoundException("Такого события нет!");
        }
        return event.get();
    }

    private EventRequest findRequestByIdOrThrow(Long requestId) {
        Optional<EventRequest> eventRequest = eventRequestRepository.findById(requestId);
        if (eventRequest.isEmpty()) {
            throw new NotFoundException("Такой заявки нет!");
        }
        return eventRequest.get();
    }

    private long getConfirmedRequests(Long eventId) {
        return eventRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }
}