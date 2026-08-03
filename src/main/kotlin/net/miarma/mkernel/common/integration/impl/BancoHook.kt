package net.miarma.mkernel.common.integration.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.miarma.mkernel.common.config.ConfigKeys
import net.miarma.mkernel.common.integration.IHook
import net.miarma.mkernel.common.service.impl.ConfigService
import ovh.mythmc.banco.api.Banco
import ovh.mythmc.banco.api.accounts.Account
import java.math.BigDecimal
import java.util.*

@Singleton
class BancoHook @Inject constructor(
    private val configService: ConfigService
) : IHook {
    override val pluginName = "banco"
    var bancoInstance: Banco? = null

    override fun register() {
        bancoInstance = Banco.get()
    }

    fun getServerAccountUuid(): UUID {
        val accountName = configService.getString(ConfigKeys.Settings.Shops.SERVER_ACCOUNT)
        return UUID.nameUUIDFromBytes("SERVER_${accountName.uppercase()}_ACCOUNT".toByteArray())
    }

    fun getOrCreateServerAccount(): Account? {
        val banco = bancoInstance ?: return null
        val accountManager = banco.accountManager

        val uuid = getServerAccountUuid()
        val accountName = configService.getString(ConfigKeys.Settings.Shops.SERVER_ACCOUNT)

        val existingAccount = accountManager.getByUuid(uuid)
        if (existingAccount != null) {
            return existingAccount
        }

        accountManager.create(uuid, accountName)
        return accountManager.getByUuid(uuid)
    }

    fun hasEnough(uuid: UUID, amount: Double): Boolean {
        val banco = bancoInstance ?: return false
        val account = banco.accountManager.getByUuid(uuid) ?: return false
        val required = BigDecimal.valueOf(amount)
        return account.amount() >= required
    }

    fun withdraw(uuid: UUID, amount: Double): Boolean {
        val banco = bancoInstance ?: return false
        val accountManager = banco.accountManager
        val account = accountManager.getByUuid(uuid) ?: return false

        val value = BigDecimal.valueOf(amount)
        if (account.amount() < value) return false

        accountManager.withdraw(account, value)
        return true
    }

    fun deposit(uuid: UUID, amount: Double): Boolean {
        val banco = bancoInstance ?: return false
        val accountManager = banco.accountManager
        val account = accountManager.getByUuid(uuid) ?: return false

        accountManager.deposit(account, BigDecimal.valueOf(amount))
        return true
    }

    fun depositToServerAccount(amount: Double): Boolean {
        val banco = bancoInstance ?: return false
        val serverUuid = getServerAccountUuid()

        getOrCreateServerAccount() ?: return false

        banco.accountManager.deposit(serverUuid, BigDecimal.valueOf(amount))
        return true
    }
}