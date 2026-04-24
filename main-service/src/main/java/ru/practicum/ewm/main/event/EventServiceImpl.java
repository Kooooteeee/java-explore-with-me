package ru.practicum.ewm.main.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryRepository;
import ru.practicum.ewm.main.exception.ConflictException;
import ru.practicum.ewm.main.exception.NotFoundException;
import ru.practicum.ewm.main.request.EventRequestRepository;
import ru.practicum.ewm.main.request.RequestStatus;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ru.practicum.ewm.stats.client.StatsClient;

import org.springframework.beans.factory.annotation.Value;
import ru.practicum.ewm.stats.dto.InputHitDto;
import ru.practicum.ewm.stats.dto.ResponseHitDto;

import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRequestRepository eventRequestRepository;
    private final StatsClient statsClient;

    @Value("${spring.application.name}")
    private String appName;

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = findUserByIdOrThrow(userId);
        Category category = findCategoryByIdOrThrow(newEventDto.getCategory());

        validateUserEventDate(newEventDto.getEventDate());

        Event event = EventMapper.toEvent(newEventDto, category, initiator);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        Event savedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(savedEvent, getConfirmedRequests(savedEvent.getId()), 0L);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        findUserByIdOrThrow(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        return events.stream()
                .map(event -> EventMapper.toEventShortDto(event, getConfirmedRequests(event.getId()), 0L))
                .toList();
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        findUserByIdOrThrow(userId);
        Event event = findByIdAndInitiatorIdOrThrow(eventId, userId);
        return EventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()), 0L);
    }

    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        findUserByIdOrThrow(userId);
        Event event = findByIdAndInitiatorIdOrThrow(eventId, userId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить опубликованное событие!");
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(findCategoryByIdOrThrow(updateEventUserRequest.getCategory()));
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getEventDate() != null) {
            validateUserEventDate(updateEventUserRequest.getEventDate());
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.getLocation() != null) {
            event.setLat(updateEventUserRequest.getLocation().getLat());
            event.setLon(updateEventUserRequest.getLocation().getLon());
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction() == UpdateEventUserAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction() == UpdateEventUserAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(updatedEvent, getConfirmedRequests(updatedEvent.getId()), 0L);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users,
                                             List<String> states,
                                             List<Long> categories,
                                             LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd,
                                             int from,
                                             int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        LocalDateTime normalizedRangeStart = normalizeAdminRangeStart(rangeStart);
        LocalDateTime normalizedRangeEnd = normalizeAdminRangeEnd(rangeEnd);

        List<Event> events = eventRepository.findAdminEvents(
                normalizeLongList(users),
                parseStates(states),
                normalizeLongList(categories),
                normalizedRangeStart,
                normalizedRangeEnd,
                pageable
        );

        return events.stream()
                .map(event -> EventMapper.toEventFullDto(event, getConfirmedRequests(event.getId()), 0L))
                .toList();
    }

    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = findEventByIdOrThrow(eventId);

        if (updateEventAdminRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.getCategory() != null) {
            event.setCategory(findCategoryByIdOrThrow(updateEventAdminRequest.getCategory()));
        }

        if (updateEventAdminRequest.getDescription() != null) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.getEventDate() != null) {
            validateAdminEventDate(updateEventAdminRequest.getEventDate());
            event.setEventDate(updateEventAdminRequest.getEventDate());
        }

        if (updateEventAdminRequest.getLocation() != null) {
            event.setLat(updateEventAdminRequest.getLocation().getLat());
            event.setLon(updateEventAdminRequest.getLocation().getLon());
        }

        if (updateEventAdminRequest.getPaid() != null) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.getTitle() != null) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == UpdateEventAdminAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Можно публиковать только событие в состоянии ожидания публикации!");
                }
                validateAdminEventDate(event.getEventDate());
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateEventAdminRequest.getStateAction() == UpdateEventAdminAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие!");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event updatedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(updatedEvent, getConfirmedRequests(updatedEvent.getId()), 0L);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               boolean onlyAvailable,
                                               String sort,
                                               int from,
                                               int size,
                                               String uri,
                                               String ip) {
        String normalizedText = normalizeText(text);
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("rangeStart не может быть позже rangeEnd");
        }

        LocalDateTime normalizedRangeStart = normalizePublicRangeStart(rangeStart);
        LocalDateTime normalizedRangeEnd = normalizePublicRangeEnd(rangeEnd);

        saveHit(uri, ip);

        if ("VIEWS".equals(sort)) {
            List<Event> events = eventRepository.findPublicEvents(
                    normalizedText,
                    normalizeLongList(categories),
                    paid,
                    normalizedRangeStart,
                    normalizedRangeEnd,
                    onlyAvailable
            );

            Map<Long, Long> viewsMap = getViewsMap(events);

            List<Event> sortedEvents = events.stream()
                    .sorted((e1, e2) -> Long.compare(
                            viewsMap.getOrDefault(e2.getId(), 0L),
                            viewsMap.getOrDefault(e1.getId(), 0L)
                    ))
                    .toList();

            int start = Math.min(from, sortedEvents.size());
            int end = Math.min(from + size, sortedEvents.size());

            return sortedEvents.subList(start, end).stream()
                    .map(event -> EventMapper.toEventShortDto(
                            event,
                            getConfirmedRequests(event.getId()),
                            viewsMap.getOrDefault(event.getId(), 0L)
                    ))
                    .toList();
        }

        Pageable pageable = createPublicPageable(from, size, sort);

        List<Event> events = eventRepository.findPublicEvents(
                normalizedText,
                normalizeLongList(categories),
                paid,
                normalizedRangeStart,
                normalizedRangeEnd,
                onlyAvailable,
                pageable
        );

        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(event -> EventMapper.toEventShortDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        viewsMap.getOrDefault(event.getId(), 0L)
                ))
                .toList();
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId, String uri, String ip) {
        Event event = findEventByIdOrThrow(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Такого события нет!");
        }

        saveHit(uri, ip);

        return EventMapper.toEventFullDto(
                event,
                getConfirmedRequests(event.getId()),
                getViews(event.getId())
        );
    }

    private Event findEventByIdOrThrow(Long eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new NotFoundException("Такого события нет!");
        }
        return event.get();
    }

    private List<EventState> parseStates(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }

        return states.stream()
                .map(EventState::valueOf)
                .toList();
    }

    private List<Long> normalizeLongList(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values;
    }

    private String normalizeText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text;
    }

    //это для сортировки, метод получался очень объемным и сложно читаемым, поэтому вынес сюда
    private Pageable createPublicPageable(int from, int size, String sort) {
        if ("EVENT_DATE".equals(sort)) {
            return PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        }
        return PageRequest.of(from / size, size);
    }

    private User findUserByIdOrThrow(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("Такого пользователя нет!");
        }
        return user.get();
    }

    private Category findCategoryByIdOrThrow(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            throw new NotFoundException("Такой категории нет!");
        }
        return category.get();
    }

    private Event findByIdAndInitiatorIdOrThrow(Long eventId, Long userId) {
        Optional<Event> event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event.isEmpty()) {
            throw new NotFoundException("Такого события нет!");
        }
        return event.get();
    }

    private long getConfirmedRequests(Long eventId) {
        return eventRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    //это для статистики
    private void saveHit(String uri, String ip) {
        InputHitDto hitDto = new InputHitDto();
        hitDto.setApp(appName);
        hitDto.setUri(uri);
        hitDto.setIp(ip);
        hitDto.setTimestamp(LocalDateTime.now());
        statsClient.saveHit(hitDto);
    }

    //это для получения статистики, я сам постоянно забываю, что он делает, поэтому тут будут пояснения
    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return new HashMap<>();
        }

        //список uri ивентов
        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        //через клиент обращаемся к сервису статистики
        List<ResponseHitDto> stats = statsClient.getStats(
                LocalDateTime.of(1970, 1, 1, 0, 0),
                LocalDateTime.now(),
                uris,
                true
        );

        //строим таблицу uri - количество просмотров
        Map<String, Long> uriToViews = new HashMap<>();
        for (ResponseHitDto stat : stats) {
            uriToViews.put(stat.getUri(), stat.getHits());
        }

        /*строим таблицу "id ивента - количество просмотров", просмотры достаем по uri из прошлого списка,
        так как uri как раз из ивентов и набрали (это был сложный момент для моей головы)*/
        Map<Long, Long> viewsMap = new HashMap<>();
        for (Event event : events) {
            viewsMap.put(event.getId(), uriToViews.getOrDefault("/events/" + event.getId(), 0L));
        }

        return viewsMap;
    }

    //это просмотры только для одного uri
    private long getViews(Long eventId) {
        List<ResponseHitDto> stats = statsClient.getStats(
                LocalDateTime.of(1970, 1, 1, 0, 0),
                LocalDateTime.now(),
                List.of("/events/" + eventId),//список из одного uri
                true
        );

        if (stats.isEmpty()) {
            return 0L;
        }

        //берем первую запись, потому что она всего одна
        return stats.get(0).getHits();
    }

    private void validateUserEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Дата события должна быть не раньше чем через 2 часа от текущего момента!");
        }
    }

    private void validateAdminEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("Дата события должна быть не раньше чем через 1 час от текущего момента!");
        }
    }

    private LocalDateTime normalizeAdminRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) {
            return LocalDateTime.of(1000, 1, 1, 0, 0);
        }
        return rangeStart;
    }

    private LocalDateTime normalizeAdminRangeEnd(LocalDateTime rangeEnd) {
        if (rangeEnd == null) {
            return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
        return rangeEnd;
    }

    private LocalDateTime normalizePublicRangeStart(LocalDateTime rangeStart) {
        if (rangeStart == null) {
            return LocalDateTime.now();
        }
        return rangeStart;
    }

    private LocalDateTime normalizePublicRangeEnd(LocalDateTime rangeEnd) {
        if (rangeEnd == null) {
            return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
        return rangeEnd;
    }
}