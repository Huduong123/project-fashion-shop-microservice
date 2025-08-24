package io.github.Huduong123.user_service.exception;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.github.Huduong123.user_service.exception.payload.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler{
    
    private static final List<String> VALIDATION_ERROR_PRIORITY = Arrays.asList(
            "notblank", // @NotBlank
            "notnull", // @NotNull
            "size", // @Size
            "pattern", // @Pattern
            "email", // @Email
            "min", // @Min
            "max", // @Max
            "decimalmin" // @DecimalMin
    // Thêm các loại lỗi khác vào đây nếu cần
    );

    /**
     * Xử lý mọi exception không xác định (Exception chung chung).
     * Trường hợp này là "bắt tất cả" — phòng ngừa lỗi không nằm trong các Exception
     * cụ thể khác.
     * Trả về mã lỗi HTTP 500 (Internal Server Error).
     *
     * @param ex Exception xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Xử lý ngoại lệ IllegalArgumentException.
     * Thường dùng khi dữ liệu đầu vào không hợp lệ (ví dụ: đăng ký trùng username,
     * email...).
     * Trả về mã lỗi HTTP 400 (Bad Request).
     *
     * @param ex IllegalArgumentException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý ngoại lệ NotFoundException (tự định nghĩa).
     * Dùng khi không tìm thấy tài nguyên, ví dụ: không tìm thấy user, sản phẩm...
     * Trả về mã lỗi HTTP 404 (Not Found).
     *
     * @param ex NotFoundException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 404
     */
    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý ngoại lệ AuthenticationFailedException (tự định nghĩa).
     * Thường xảy ra khi tài khoản hoặc mật khẩu không chính xác.
     * Trả về mã lỗi HTTP 401 (Unauthorized).
     *
     * @param ex AuthenticationFailedException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 401
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<?> handleAuthenticationFailed(AuthenticationFailedException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý ngoại lệ BadCredentialsException.
     * Thường xảy ra khi username hoặc password không đúng.
     * Trả về mã lỗi HTTP 401 (Unauthorized).
     *
     * @param ex BadCredentialsException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 401
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid username or password");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý ngoại lệ AuthenticationException chung.
     * Trả về mã lỗi HTTP 401 (Unauthorized).
     *
     * @param ex AuthenticationException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 401
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthentication(AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication failed: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");

        // Bước 1: Nhóm TẤT CẢ các lỗi theo tên trường.
        // Một trường có thể có nhiều lỗi (ví dụ: vừa NotBlank vừa Size).
        Map<String, List<FieldError>> errorsGroupedByField = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(FieldError::getField));

        // Bước 2: Lấy đối tượng DTO để xác định thứ tự các trường
        Object target = ex.getBindingResult().getTarget();
        List<String> orderedMessages = new ArrayList<>();

        if (target != null) {
            // Bước 3: Dùng Reflection để duyệt qua các trường theo thứ tự đã khai báo
            Field[] declaredFields = target.getClass().getDeclaredFields();

            for (Field field : declaredFields) {
                String fieldName = field.getName();

                // Xử lý trường hợp field có @JsonProperty
                if (field.isAnnotationPresent(JsonProperty.class)) {
                    JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                    if (!jsonProperty.value().isEmpty()) {
                        fieldName = jsonProperty.value();
                    }
                }

                // Nếu trường này có lỗi trong danh sách đã nhóm
                if (errorsGroupedByField.containsKey(fieldName)) {
                    // Lấy danh sách lỗi của trường này
                    List<FieldError> fieldErrors = errorsGroupedByField.get(fieldName);
                    // Chọn ra lỗi có độ ưu tiên cao nhất
                    String highestPriorityMessage = getHighestPriorityMessage(fieldErrors);
                    if (highestPriorityMessage != null) {
                        orderedMessages.add(highestPriorityMessage);
                    }
                }
            }
        } else {
            // Fallback: Nếu không lấy được DTO, chỉ trả về các lỗi không theo thứ tự
            orderedMessages.addAll(
                    errorsGroupedByField.values().stream()
                            .map(fieldErrors -> getHighestPriorityMessage(fieldErrors))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
        }

        body.put("messages", orderedMessages);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Chọn ra thông báo lỗi quan trọng nhất từ danh sách các lỗi của một trường,
     * dựa trên danh sách ưu tiên VALIDATION_ERROR_PRIORITY.
     * 
     * @param fieldErrors Danh sách các lỗi của một trường cụ thể.
     * @return Thông báo lỗi có độ ưu tiên cao nhất, hoặc null nếu không tìm thấy.
     */
    private String getHighestPriorityMessage(List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return null;
        }

        // Duyệt qua danh sách ưu tiên đã định nghĩa
        for (String priority : VALIDATION_ERROR_PRIORITY) {
            // Tìm lỗi đầu tiên trong danh sách của trường khớp với độ ưu tiên
            for (FieldError error : fieldErrors) {
                // error.getCodes() trả về mảng như ["NotBlank.dto.username",
                // "NotBlank.username", "NotBlank"]
                // Mã lỗi cuối cùng thường là tên annotation viết thường.
                String errorCode = error.getCodes()[error.getCodes().length - 1];
                if (priority.equalsIgnoreCase(errorCode)) {
                    return error.getDefaultMessage(); // Trả về ngay khi tìm thấy lỗi ưu tiên cao nhất
                }
            }
        }

        // Nếu không có lỗi nào trong danh sách ưu tiên, trả về lỗi đầu tiên trong danh
        // sách
        return fieldErrors.get(0).getDefaultMessage();
    }

    /**
     * Xử lý ngoại lệ khi kiểu dữ liệu của một tham số trong request không hợp lệ.
     * Ví dụ: người dùng nhập chữ vào một trường yêu cầu số (Integer, Long,
     * BigDecimal).
     *
     * @param ex MethodArgumentTypeMismatchException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String fieldName = ex.getName();
        String requiredType = ex.getRequiredType().getSimpleName();
        Object invalidValue = ex.getValue();

        String message = String.format(
                "Giá trị '%s' không hợp lệ cho trường '%s'. Vui lòng nhập một giá trị kiểu '%s'.",
                invalidValue, fieldName, requiredType);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

     /**
     * THÊM MỚI: Xử lý ngoại lệ DuplicateResourceException (tự định nghĩa).
     * Dùng khi cố gắng tạo một tài nguyên đã tồn tại trong hệ thống (ví dụ: trùng code, name, email).
     * Trả về mã lỗi HTTP 409 (Conflict).
     *
     * @param ex DuplicateResourceException xảy ra
     * @return ResponseEntity chứa ErrorResponse và mã lỗi 409
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicateResource(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
