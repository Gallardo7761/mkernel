package net.miarma.mkernel.miarmacraft.common.integration.impl

import com.google.inject.Inject
import com.google.inject.Singleton
import net.leonardo_dgs.interactivebooks.IBook
import net.leonardo_dgs.interactivebooks.InteractiveBooks
import net.miarma.mkernel.MKernel
import net.miarma.mkernel.api.common.IHook
import net.miarma.mkernel.miarmacraft.common.dao.CrimeDao
import java.io.File

@Singleton
class InteractiveBooksHook @Inject constructor(
    private val plugin: MKernel,
    private val crimeDao: CrimeDao
) : IHook {
    override val pluginName: String = "InteractiveBooks"

    override fun register() {
        syncBooks()
    }

    fun syncBooks() {
        if (!isInstalled()) return

        plugin.launchAsync {
            val categories = crimeDao.getAllCategories()
            val articles = crimeDao.getAllArticles()

            plugin.launchSync {
                val ibFolder = File(InteractiveBooks.getInstance().dataFolder, "books")

                val existingIds = InteractiveBooks.getBooks().keys.filter { it.startsWith("cp_") }.toList()
                for (id in existingIds) {
                    InteractiveBooks.unregisterBook(id)
                    val oldFile = File(ibFolder, "$id.yml")
                    if (oldFile.exists()) oldFile.delete()
                }

                for (category in categories) {
                    val catArticles = articles.filter { it.categoryId == category.id }

                    val safeCategoryName = category.name.lowercase().replace(Regex("[^a-z0-9]"), "_")
                    val bookId = "cp_$safeCategoryName"
                    val openCommand = safeCategoryName

                    val author = "RDH - República Dictatorial de Hispania"
                    val lore = listOf(
                        "<gray>En nombre de la república, se protege</gray>",
                        "<gray>la integridad y la vida de todo</gray>",
                        "<gray>habitante del territorio.</gray>"
                    )

                    val pages = mutableListOf<String>()

                    pages.add("<dark_red><bold>${category.name}</bold></dark_red>\n\n<black>Artículos y regulaciones correspondientes a esta categoría del Código Penal.</black>")

                    for (article in catArticles) {
                        val fineText = if (article.fineAmount > 0) {
                            "\n\n<dark_red>Multa:</dark_red> <b>${article.fineAmount}</b> <hover:show_item:emerald:1><dark_green>♦</dark_green></hover>"
                        } else ""

                        pages.add("<bold>${article.name}</bold>\n\n<black>${article.description}</black>$fineText")
                    }

                    val book = IBook(
                        bookId,
                        category.name,
                        category.name,
                        author,
                        "ORIGINAL",
                        lore,
                        pages,
                        openCommand
                    )

                    InteractiveBooks.registerBook(book)
                    book.save()
                }

                plugin.logger.info("Synchronized ${categories.size} categories with InteractiveBooks!")
            }
        }
    }
}