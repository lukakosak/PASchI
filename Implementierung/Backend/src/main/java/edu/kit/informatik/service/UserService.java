package edu.kit.informatik.service;

import edu.kit.informatik.dto.UserDto;
import edu.kit.informatik.dto.mapper.UserMapper;
import edu.kit.informatik.exceptions.EntityNotFoundException;
import edu.kit.informatik.model.User;
import edu.kit.informatik.model.userdata.courses.Course;
import edu.kit.informatik.model.userdata.interactions.Category;
import edu.kit.informatik.model.userdata.interactions.Participant;
import edu.kit.informatik.model.userdata.rooms.Room;
import edu.kit.informatik.repositories.CategoryBaseRepository;
import edu.kit.informatik.repositories.CourseRepository;
import edu.kit.informatik.repositories.ParticipantRepository;
import edu.kit.informatik.repositories.RoomRepository;
import edu.kit.informatik.repositories.UserRepository;
import edu.kit.informatik.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service für {@link User Benutzer}
 *
 * @author ugqbo
 * @version 1.0
 */

@Component
public class UserService extends BaseService<User, UserDto, UserDto> {
    /**
     * Message, wenn eine E-Mail bereits verwendet wurde
     */
    public static final String EMAIL_ALREADY_EXITS = "EMAIL_ALREADY_EXITS";

    private final UserRepository userRepository;
    private final CourseService courseService;
    private final RoomService roomService;
    private final CategoryService categoryService;
    private final ParticipantService participantService;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final CategoryBaseRepository<Category> categoryBaseRepository;
    private final ParticipantRepository participantRepository;

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    /**
     * Konstruktor zum Erstellen eines Objektes der Klasse
     *
     * @param userRepository         {@link UserRepository}
     * @param userMapper             {@link UserMapper}
     * @param courseService          {@link CourseService}
     * @param roomService            {@link RoomService}
     * @param categoryService        {@link CategoryService}
     * @param participantService     {@link ParticipantService}
     * @param courseRepository       {@link CourseRepository}
     * @param roomRepository         {@link RoomRepository}
     * @param categoryBaseRepository {@link CategoryBaseRepository}
     * @param participantRepository  {@link ParticipantRepository}
     * @param tokenService           {@link TokenService}
     * @param authenticationManager  {@link AuthenticationManager}
     */
    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper,
                       CourseService courseService, RoomService roomService, CategoryService categoryService,
                       ParticipantService participantService, CourseRepository courseRepository,
                       RoomRepository roomRepository, CategoryBaseRepository<Category> categoryBaseRepository,
                       ParticipantRepository participantRepository, TokenService tokenService,
                       AuthenticationManager authenticationManager) {
        super(userMapper);
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.roomService = roomService;
        this.categoryService = categoryService;
        this.participantService = participantService;
        this.courseRepository = courseRepository;
        this.roomRepository = roomRepository;
        this.categoryBaseRepository = categoryBaseRepository;
        this.participantRepository = participantRepository;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDto add(UserDto userDto, Authentication authentication) {
        User user = this.mapper.dtoToModel(userDto);

        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());

        if (userOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, EMAIL_ALREADY_EXITS);
        }

        String newPassword = new BCryptPasswordEncoder().encode(user.getPassword());

        user.setPassword(newPassword);

        User newUser = userRepository.save(user);

        return this.mapper.modelToDto(newUser);
    }

    @Transactional
    @Override
    public UserDto update(UserDto userDto, Authentication authentication) {
        super.checkAuthorization(authentication, userDto.getId());
        Optional<User> repositoryUserOptional = userRepository.findUserById(userDto.getId());

        User repositoryUser = repositoryUserOptional
                .orElseThrow(() -> new EntityNotFoundException(User.class, userDto.getId()));
        User newUser = this.mapper.dtoToModel(userDto);

        if (!newUser.getFirstName().equals(repositoryUser.getFirstName())) {
            repositoryUser.setFirstName(newUser.getFirstName());
        }
        if (!newUser.getLastName().equals(repositoryUser.getLastName())) {
            repositoryUser.setLastName(newUser.getLastName());
        }
        if (!newUser.getPassword().equals(repositoryUser.getPassword())) {
            repositoryUser.setPassword(newUser.getPassword());
        }

        return mapper.modelToDto(repositoryUser);
    }

    @Override
    public UserDto getById(String id, Authentication authentication) {
        Optional<User> userOptional = userRepository.findUserById(id);

        User user = userOptional.orElseThrow(() -> new EntityNotFoundException(User.class, id));

        return this.mapper.modelToDto(user);
    }

    @Override
    public List<UserDto> getAll(Authentication authentication) {
        return this.mapper.modelToDto(userRepository.findAll());
    }

    @Override
    public String delete(String id, Authentication authentication) {
        Optional<User> repositoryUserOptional = userRepository.findUserById(id);
        repositoryUserOptional.orElseThrow(() -> new EntityNotFoundException(User.class, id));

        List<Course> courses = courseRepository.findCoursesByUserId(id);
        for (Course course: courses) {
            courseService.delete(course);
        }

        List<Category> categories = categoryBaseRepository.findCategoryByUserId(id);
        for (Category category: categories) {
            categoryService.delete(category);
        }

        List<Room> rooms = roomRepository.findRoomsByUserId(id);
        for (Room room: rooms) {
            roomService.delete(room);
        }

        List<Participant> participants = participantRepository.findParticipantsByUserId(id);
        for (Participant participant: participants) {
            participantService.delete(participant);
        }

        this.userRepository.deleteById(id);

        return id;
    }

    /**
     * Login des Benutzers
     * @param email E-Mail
     * @param password Password
     * @return {@link UserDto}
     */
    public UserDto login(String email, String password) throws DisabledException, LockedException,
                                                                BadCredentialsException {
        UsernamePasswordAuthenticationToken uPAT = new UsernamePasswordAuthenticationToken(email, password);

        Authentication authentication = authenticationManager.authenticate(uPAT);

        User user = (User) authentication.getPrincipal();
        UserDto userDto = this.mapper.modelToDto(user);
        userDto.setToken(tokenService.generateToken(authentication, user));

        return userDto;
    }

    /**
     * Rückgabe eines neuen JWT-Tokens bei Authentifizierung
     * @param authentication {@link Authentication}
     * @return {@link UserDto}
     */
    public UserDto getToken(Authentication authentication) {
        JwtAuthenticationToken jAT = (JwtAuthenticationToken) authentication;

        String userId = jAT.getTokenAttributes().get("userId").toString();

        Optional<User> repositoryUserOptional = userRepository.findUserById(userId);

        User repositoryUser = repositoryUserOptional.orElseThrow(() -> new EntityNotFoundException(
                User.class, userId));

        UserDto userDto = this.mapper.modelToDto(repositoryUser);
        userDto.setToken(tokenService.generateToken(authentication, repositoryUser));

        return userDto;
    }

    /**
     * Methode zum Verändern der Attribute 'auth' und 'role' des Benutzers in der Datenbank
     * @param userDto {@link UserDto}
     * @return {@link UserDto}
     * @throws EntityNotFoundException falls {@link User} mit der übergebenen Id nicht gefunden wurde
     */
    @Transactional
    public UserDto adminUpdate(UserDto userDto) throws EntityNotFoundException {
        Optional<User> repositoryUserOptional = userRepository.findUserById(userDto.getId());

        User repositoryUser = repositoryUserOptional.orElseThrow(() ->
                                                            new EntityNotFoundException(User.class, userDto.getId()));
        User newUser = this.mapper.dtoToModel(userDto);

        if (!newUser.isAuth() == repositoryUser.isAuth()) {
            repositoryUser.setAuth(newUser.isAuth());
        }
        if (newUser.getRole() != repositoryUser.getRole()) {
            repositoryUser.setRole(newUser.getRole());
        }

        return mapper.modelToDto(repositoryUser);
    }
}
