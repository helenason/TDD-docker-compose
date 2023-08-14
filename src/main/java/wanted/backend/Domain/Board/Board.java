package wanted.backend.Domain.Board;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import wanted.backend.Domain.BaseTimeEntity;
import wanted.backend.Domain.Member.Member;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @ManyToOne
    @JoinColumn(name = "writer")
    private Member writer;

    @NotNull
    @ColumnDefault("false")
    private Boolean isUpdated;

    @Builder
    public Board(String title, String content, Member writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isUpdated = false;
    }

    public void updateBoard(String title, String content) {
        this.title = title;
        this.content = content;
        this.isUpdated = true;
    }
}
