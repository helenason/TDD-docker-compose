package wanted.backend.Domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ResponseDto {

    private HttpStatus status;
    private String message;
    private Object data;

    public ResponseDto() {
        this.status = HttpStatus.OK;
        this.message = "Success";
    }

}
