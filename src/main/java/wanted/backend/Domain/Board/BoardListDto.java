package wanted.backend.Domain.Board;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardListDto {

    private Long id;
    private String title;
    private LocalDateTime date;
    private String writer;

}
