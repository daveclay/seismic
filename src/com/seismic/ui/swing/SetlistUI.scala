package com.seismic.ui.swing

import java.awt.event.KeyListener
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.ui.swing.draglist.{ListCallbacks, OrderableSelectionList}
import com.seismic.{Phrase, Seismic, SetList, Song}

class SetlistUI(seismic: Seismic,
                callbacks: SeismicSerialCallbacks,
                size: Dimension,
                backgroundColor: Color,
                componentBGColor: Color) extends JPanel {

  setPreferredSize(size)

  seismic.onPhraseChange { (phrase: Phrase) =>
    indicateSelectedSong(phrase.song)
    indicateSelectedPhrase(phrase)
  }

  val namePanel = new JPanel
  namePanel.setPreferredSize(new Dimension(size.width - 8, 30))
  namePanel.setBackground(componentBGColor)
  SwingComponents.addBorder(namePanel)
  val nameField = new LabeledTextField("Set List", 12, onSetListNameChange)

  val songCallbacks = ListCallbacks(songClicked,
                                    songBackout,
                                    addSong,
                                    songsReordered)
  val songSelect = new OrderableSelectionList[Song](songCallbacks, Selector.renderSongItem)
  songSelect.setPreferredSize(new Dimension(250, size.height - 4))
  songSelect.setBackground(componentBGColor)

  val phraseCallbacks = new ListCallbacks(phraseClicked,
                                          phraseBackout,
                                          addPhrase,
                                          phrasesReordered)
  val phraseSelect = new OrderableSelectionList[Phrase](phraseCallbacks, Selector.renderPhraseItem)
  phraseSelect.setPreferredSize(new Dimension(250, size.height - 4))
  phraseSelect.setBackground(componentBGColor)

  val selector = new Selector(songSelect, phraseSelect)
  selector.setOpaque(false)

  private val editorWidth = size.width - songSelect.getPreferredSize.width - phraseSelect.getPreferredSize.width - 12

  val songEditor = new SongEditor(onSongUpdated, new Dimension(editorWidth, 40), componentBGColor)
  val phraseEditor = new PhraseEditor(save,
                                       onPhraseUpdated,
                                       onPhraseDeleted,
                                       new Dimension(editorWidth, size.height - 40),
                                       componentBGColor)

  val editor = new Editor(songEditor, phraseEditor)
  editor.setOpaque(false)

  position(nameField).at(4, 4).in(namePanel)
  position(namePanel).at(0, 4).in(this)
  position(selector).below(namePanel).withMargin(4).in(this)
  position(editor).toTheRightOf(selector).withMargin(4).in(this)

  def save(): Unit = {
    seismic.save()
  }

  override def addKeyListener(keyListener: KeyListener): Unit = {
    super.addKeyListener(keyListener)
    songSelect.addKeyListener(keyListener)
    phraseSelect.addKeyListener(keyListener)
    selector.addKeyListener(keyListener)
  }

  def onSetListNameChange(name: String): Unit = {
    seismic.setListOpt.foreach {
      setList => {
        setList.setName(name)
      }
    }
  }

  def songClicked(song: Song): Unit = {
    seismic.setCurrentSong(song)
  }

  def addSong(): Unit = {
    seismic.setListOpt.foreach { setList =>
      val song = setList.addSong()
      save()
      songSelect.addItem(song)
      // TODO: order is important here :( - setCurrentSong will fire the phraseSelected callback, but the item won't be in the list yet.
      seismic.setCurrentSong(song)

      phraseSelect.grabFocus()
    }
  }

  def songBackout(): Unit = {
    //TODO?
  }

  def onSongUpdated(song: Song): Unit = {
    save()
    songSelect.itemWasUpdated(song)
  }

  def songsReordered(songs: Seq[Song]): Unit = {
    seismic.setListOpt.foreach { setList =>
      setList.updateSongs(songs)
      seismic.save()
    }
  }

  def addPhrase(): Unit = {
    seismic.currentSongOpt.foreach { song =>
      val phrase = song.addPhrase()
      save()
      phraseSelect.addItem(phrase)
      seismic.setCurrentPhrase(phrase)
      phraseEditor.grabFocus()
    }
  }

  def onPhraseDeleted(phrase: Phrase): Unit = {
    seismic.currentSongOpt.foreach { song =>
      val phraseIdx = song.phrases.indexOf(phrase)
      val phraseToSelect = if (phraseIdx < 1) song.phrases(0) else song.phrases(phraseIdx - 1)

      song.removePhrase(phrase)
      save()
      seismic.setCurrentPhrase(phraseToSelect)
      phraseSelect.setItems(song.phrases)
      phraseSelect.setCurrentSelectedItem(phraseToSelect)
      phraseEditor.setPhrase(phraseToSelect)
    }
  }

  def onPhraseUpdated(phrase: Phrase): Unit = {
    save()
    phraseSelect.itemWasUpdated(phrase)
  }

  def phraseClicked(phrase: Phrase): Unit = {
    seismic.setCurrentPhrase(phrase)
    // TODO: when to focus on edit? play/edit mode toggle? phraseEditor.grabFocus()
  }

  def phraseBackout(): Unit = {
    // TODO: eh, what do I want to do here?
  }

  def phrasesReordered(phrases: Seq[Phrase]): Unit = {
    seismic.currentSongOpt.foreach { song =>
      song.updatePhrases(phrases)
      seismic.save()
    }
  }

  private def indicateSelectedSong(song: Song): Unit = {
    val phrases = song.phrases
    val phrase = phrases.head
    songSelect.setCurrentSelectedItem(song)
    songEditor.setSong(song)
    phraseSelect.setItems(phrases)
  }

  private def indicateSelectedPhrase(phrase: Phrase): Unit = {
    phraseEditor.setPhrase(phrase)
    phraseSelect.setCurrentSelectedItem(phrase)
  }

  def newSetList(): Unit = {
    setSetList(seismic.newSetList)
  }

  def openSetList(setList: SetList): Unit = {
    setSetList(setList)
    phraseSelect.grabFocus()
  }

  def setSetList(setList: SetList): Unit = {
    val song = setList.songs.head
    val phrase = song.phrases.head

    nameField.setText(setList.name)
    songSelect.setItems(setList.songs)

    indicateSelectedSong(song)
    indicateSelectedPhrase(phrase)
  }
}


class Editor(songEditor: SongEditor,
             phraseEditor: PhraseEditor) extends JPanel {

  setPreferredSize(new Dimension(Sizing.fitWidth(songEditor), Sizing.fitHeight(songEditor, phraseEditor)))

  position(songEditor).atOrigin().in(this)
  position(phraseEditor).below(songEditor).withMargin(4).in(this)
}

