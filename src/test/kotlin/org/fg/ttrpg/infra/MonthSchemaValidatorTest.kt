package org.fg.ttrpg.infra

import org.fg.ttrpg.infra.validation.MonthSchemaValidator
import org.fg.ttrpg.infra.validation.MonthSchemaValidationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import org.junit.jupiter.api.Test

class MonthSchemaValidatorTest {
    private val validator = MonthSchemaValidator()

    @Test
    fun `invalid schema throws`() {
        val bad = """[{"name":"A"}]"""
        shouldThrow<MonthSchemaValidationException> {
            validator.validate(bad)
        }
    }

    @Test
    fun `valid schema passes`() {
        val ok = """[{"name":"Jan","days":31}]"""
        shouldNotThrowAny { validator.validate(ok) }
    }
}
