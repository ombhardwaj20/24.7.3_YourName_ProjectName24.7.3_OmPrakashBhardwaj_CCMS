package com.card.Credit_card.contoller;

import com.card.Credit_card.configuration.SpringFoxConfiguration;
import com.card.Credit_card.dto.LoginDto;
import com.card.Credit_card.dto.LoginResponseDto;
import com.card.Credit_card.dto.SignUpDto;
import com.card.Credit_card.dto.UserDto;
import com.card.Credit_card.model.ErrorDetails;
import com.card.Credit_card.security.JwtConfig;
import com.card.Credit_card.security.JwtUtil;
import com.card.Credit_card.services.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;



@RestController
@Validated
@Slf4j
@CrossOrigin
@Api(tags = SpringFoxConfiguration.USER_TAG)
@SwaggerDefinition(
        info = @Info(description = "User operations.", title = "User Controller", version = "1.0")
)
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    private final JwtConfig jwtConfig;

    /**
     * Logs in the user.
     *
     * @param loginDTO the login dto
     * @return the response entity
     */
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Successfully authenticated", response = LoginResponseDto.class),
            @ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "Incorrect credentials")
    })
    @ApiOperation(value = "Logs in the user with the given credentials.", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginDto loginDTO) {
        logger.trace("Entered login");
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmailId(),
                            loginDTO.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String emailId = userDetails.getUsername();
            Optional<UserDto> userByEmailId = userService.getUserByEmailId(emailId);
            if (userByEmailId.isPresent()) {
                LoginResponseDto loginResponseDTO =
                        modelMapper.map(userByEmailId.get(), LoginResponseDto.class);
                loginResponseDTO.setTokenType(jwtConfig.getTokenPrefix().trim());
                String jwtToken = jwtUtil.generateJwtToken(emailId);
                loginResponseDTO.setToken(jwtToken);
                logger.trace("Exited login");
                return ResponseEntity.ok(loginResponseDTO);
            }
            return ResponseEntity.notFound().build();
        } catch (AuthenticationException authenticationException) {
            logger.trace("Exited login");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Signs up the user.
     *
     * @param signUpDTO the sign up dto
     * @return the response entity
     */
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_CREATED, message = "User created successfully.", response = UserDto.class),
            @ApiResponse(code = HttpServletResponse.SC_CONFLICT, message = "Email id already exists", response = ErrorDetails.class)
    })
    @ApiOperation(value = "Registers the user with the given details.", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpDto signUpDTO) {
        logger.trace("Entered signUp");

        if (userService.isUserExists(signUpDTO.getEmailId()))
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDetails(HttpStatus.CONFLICT, "Email id already exists."));
        Optional<UserDto> signUpResponseDTO = userService.signUpUser(signUpDTO);
        if (signUpResponseDTO.isPresent()) {
            UserDto user = signUpResponseDTO.get();
            String jwtToken = jwtUtil.generateJwtToken(user.getEmailId());
            user.setToken(jwtToken);
            user.setTokenType("Bearer");
            logger.trace("Exited signUp");
            return ResponseEntity.created(URI.create("/user/" + user.getUserId())).body(user);
        }
        logger.trace("Exited signUp");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    /**
     * Gets user by id.
     *
     * @param userId the user id
     * @return the user details if found
     */
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_OK, message = "Found the user details", response = UserDto.class),
            @ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "User with given user id not found.")
    })
    @ApiOperation(value = "Gets the user details by user id.",
            produces = MediaType.APPLICATION_JSON_VALUE, response = UserDto.class,
            authorizations = {@Authorization("JWT")})
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUserById(@PathVariable(value = "userId") UUID userId) {
        logger.trace("Entered getUserById");
        Optional<UserDto> userById = userService.getUserById(userId);
        logger.trace("Exited getUserById");
        return ResponseEntity.of(userById);
    }

    /**
     * Delete user by id response entity.
     *
     * @param userId the user id
     * @return the response entity
     */
    @ApiResponses({
            @ApiResponse(code = HttpServletResponse.SC_NO_CONTENT, message = "Deleted the user successfully."),
            @ApiResponse(code = HttpServletResponse.SC_NOT_FOUND, message = "User with given user id not found.")
    })
    @ApiOperation(value = "Deletes the user by user id.", authorizations = {@Authorization("JWT")})
    @DeleteMapping(value = "/user/{userId}")
    public ResponseEntity<?> deleteUserById(@PathVariable(value = "userId") UUID userId) {
        if (userService.isUserExists(userId)) {
            userService.deleteUserById(userId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}