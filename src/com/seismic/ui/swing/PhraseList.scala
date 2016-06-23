package com.seismic.ui.swing

import java.awt.{Color, Dimension}
import javax.swing.{JButton, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.Phrase

class PhraseList(onPhraseSelected: (Phrase) => Unit,
                 onEditPhraseSelected: (Phrase) => Unit,
                 onAddPhraseSelected: () => Unit,
                 backgroundColor: Color) extends JPanel() {

  SwingComponents.addBorder(this)
  setPreferredSize(new Dimension(250, 400))
  setBackground(backgroundColor)

  var addPhraseButton = new JButton("Add Phrase")
  addPhraseButton.addActionListener(e => { onAddPhraseSelected() })
  var phraseItemsOpt: Option[Seq[PhraseItem]] = None

  def addPhrase(phrase: Phrase): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      val phraseItem = createPhraseItem(phrase)
      val all = phraseItems :+ phraseItem
      phraseItemsOpt = Option(all)
      indicateSelectedItem(phraseItem)
    }
    layoutPhraseItems()
  }

  def setPhrases(phrases: Seq[Phrase]): Unit = {
    phraseItemsOpt = Option(phrases.map { phrase =>
      createPhraseItem(phrase)
    })
    layoutPhraseItems()
  }

  def findPhraseFunc(phrase: Phrase, f: (PhraseItem) => Unit) = {
    () => findPhraseItemForPhrase(phrase).foreach { phraseItem => f(phraseItem) }
  }

  private def indicateSelectedItem(phraseItem: PhraseItem): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItems.foreach { item => item.indicateUnselect() }
      phraseItem.indicateSelected()
    }
  }

  private def indicateSelectedPhrase(phrase: Phrase): Unit = {
    findPhraseItemForPhrase(phrase).foreach { phraseItem => indicateSelectedItem(phraseItem) }
  }

  private def createPhraseItem(phrase: Phrase) = {
    val selectPrevious = findPhraseFunc(phrase, { phraseItem => selectPreviousFrom(phraseItem) })
    val selectNext = findPhraseFunc(phrase, { phraseItem => selectNextFrom(phraseItem) })
    val onShowPhrase = () => {
      onPhraseSelected(phrase)
      indicateSelectedPhrase(phrase)
    }
    val onEditPhrase = () => {
      onEditPhraseSelected(phrase)
      indicateSelectedPhrase(phrase)
    }
    new PhraseItem(phrase, onShowPhrase, onEditPhrase, selectPrevious, selectNext)
  }

  def phraseWasUpdated(phrase: Phrase): Unit = {
    findPhraseItemForPhrase(phrase) match {
      case Some(item) => item.setLabel(phrase.name)
      case None => println(s"Could not find phrase to update")
    }
  }

  private def selectPreviousFrom(phraseItem: PhraseItem): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      val index = phraseItems.indexOf(phraseItem)
      selectItemAt(wrapIndex(index - 1, phraseItems))
    }
  }

  private def selectNextFrom(phraseItem: PhraseItem): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
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
      phraseItems.head.grabFocus()
    }
  }

  private def selectLast(): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      phraseItems.last.grabFocus()
    }
  }

  private def selectItemAt(index: Int): Unit = {
    phraseItemsOpt.foreach { phraseItems =>
      selectItem(phraseItems(index))
    }
  }

  private def selectItem(item: PhraseItem): Unit = {
    item.grabFocus()
    indicateSelectedItem(item)
  }

  private def findPhraseItemForPhrase(phraseToFind: Phrase) = {
    phraseItemsOpt.flatMap { phraseItems =>
      // TODO: nope, they're case classes, it just does the name equality. two phrases named the same are bad. prevent it.
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

      position(addPhraseButton).below(lastPhraseItem).withMargin(4).in(this)
    }
  }
}
