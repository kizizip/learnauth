package ssafy.d210.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorContentDto {

    private String message;
}
