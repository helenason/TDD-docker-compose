package wanted.backend.Domain.Board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class BoardDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        private String title;
        private String content;

    }

    public static class Response {

        private Long id;
        private String title;
        private String content;
        private String writer_email;

    }
}
