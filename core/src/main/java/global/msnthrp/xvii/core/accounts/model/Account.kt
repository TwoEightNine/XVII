package global.msnthrp.xvii.core.accounts.model

data class Account(
        val userId: Int,
        val token: String,
        val name: String,
        val photo: String? = null,
        val isActive: Boolean = false
)