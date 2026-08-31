package ch.nickl.o11y.app.application.usecase;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static ch.nickl.o11y.app.application.usecase.BusinessUseCase.parseDelayToMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DelayParsingTest {

    @ParameterizedTest(name = "\"{0}\" -> {1} ms")
    @CsvSource({
            "250ms,     250",
            "2s,        2000",
            "1.5s,      1500",
            "1m,        60000",
            "1.5m,      90000",
            "1h,        3600000",
            "300,       300",      // bare number is interpreted as milliseconds
            "PT2S,      2000",
            "PT0.5S,    500",
            "pt2s,      2000",     // case-insensitive
            "'  2s  ',  2000",     // surrounding whitespace is trimmed
    })
    void given_aSupportedDelayFormat_when_parsed_then_returnsTheExpectedMilliseconds(String input, long expectedMillis) {
        assertThat(parseDelayToMillis(input)).isEqualTo(expectedMillis);
    }

    @ParameterizedTest(name = "\"{0}\" is clamped to 1 ms")
    @ValueSource(strings = {"0", "0ms", "0s"})
    void given_aNonPositiveDelay_when_parsed_then_isClampedToOneMillisecond(String input) {
        assertThat(parseDelayToMillis(input)).isEqualTo(1L);
    }

    @ParameterizedTest(name = "rejects \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "oops", "5min", "2 seconds", "s", "ms", "PTOOPS", "1x"})
    void given_anUnparseableDelay_when_parsed_then_throwsBadRequest(String input) {
        assertThatThrownBy(() -> parseDelayToMillis(input))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void given_anUnparseableDelay_when_parsed_then_theErrorMessageEchoesTheInputAndHintsAtValidFormats() {
        assertThatThrownBy(() -> parseDelayToMillis("nonsense"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nonsense")
                .hasMessageContaining("PT2S");
    }
}
