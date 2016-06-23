package com.seismic.ui.swing

import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.Phrase

class PhraseList(onPhraseSelected: (Phrase) => Unit,
                 onPhraseAdded: () => Unit,
                 onEditPhraseClicked: () => Unit,
                 backgroundColor: Color) extends JPanel() {

  setPreferredSize(new Dimension(140, 400))
  setBackground(backgroundColor)

  var addPhraseItem = new AddPhraseItem(onPhraseAdded, selectLast, selectFirst)
  var phraseItemsOpt: Option[Seq[SelectPhraseItem]] = None

  def addPhrase(phrase: Phrase): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      val phraseItem = createPhraseItem(phrase)
      phraseItemsOpt = Option(phraseItems :+ phraseItem)
    }
    layoutPhraseItems()
  }

  def setPhrases(phrases: Seq[Phrase]): Unit = {
    phraseItemsOpt = Option(phrases.map { phrase =>
      createPhraseItem(phrase)
    })
    layoutPhraseItems()
  }

  private def createPhraseItem(phrase: Phrase) = {
    val selectPrevious = () => findPhraseItemForPhrase(phrase).foreach { phraseItem => selectPreviousFrom(phraseItem) }
    val selectNext = () => findPhraseItemForPhrase(phrase).foreach { phraseItem => selectNextFrom(phraseItem) }
    val selected = () => {
      onPhraseSelected(phrase)
    }
    new SelectPhraseItem(phrase, selected, onEditPhraseClicked, selectPrevious, selectNext)
  }

  def phraseWasUpdated(phrase: Phrase): Unit = {
    findPhraseItemForPhrase(phrase) match {
      case Some(item) => item.setLabel(phrase.name)
      case None => println(s"Could not find phrase to update")
    }
  }

  private def selectPreviousFrom(phraseItem: SelectPhraseItem): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItem.unselect()
      val index = phraseItems.indexOf(phraseItem)
      selectItemAt(wrapIndex(index - 1, phraseItems))
    }
  }

  private def selectNextFrom(phraseItem: PhraseItem): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItem.unselect()
      val index = phraseItems.indexOf(phraseItem)
      selectItemAt(wrapIndex(index + 1, phraseItems))
    }
  }

  private def wrapIndex(index: Int, array: Seq[Any]) = {
    if (index < 0) {
      array.size - 1
    } else if (index >= array.size) {
      0
    } else {
      index
    }
  }

  private def selectFirst(): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItems.head.select()
    }
  }

  private def selectLast(): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItems.last.select()
    }
  }

  private def selectItemAt(index: Int): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItems(index).select()
    }
  }

  private def findPhraseItemForPhrase(phraseToFind: Phrase) = {
    phraseItemsOpt.flatMap { phraseItems =>
      phraseItems.find { item => item.phrase.equals(phraseToFind) }
    }
  }

  private def layoutPhraseItems(): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      val firstPhraseItem = phraseItems.head
      position(firstPhraseItem).atOrigin().in(this)
      val lastPhraseItem = phraseItems.foldLeft(firstPhraseItem) { (itemAbove, phraseItem) =>
        position(phraseItem).below(itemAbove).withMargin(4).in(this)
        phraseItem
      }

      position(addPhraseItem).below(lastPhraseItem).withMargin(4).in(this)
    }
  }
}
