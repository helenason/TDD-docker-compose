package wanted.backend.Domain.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {

    private String accessToken;
    private String refreshToken;

}
