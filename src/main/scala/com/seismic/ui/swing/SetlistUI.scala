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
  namePanel.setPreferredSize(new Dimension(size.width - 4, 30))
  namePanel.setBackground(componentBGColor)
  SwingComponents.addBorder(namePanel)
  val nameField = new LabeledTextField("Set List", 40, onSetListNameChange)

  val songCallbacks = ListCallbacks(songClicked,
                                    songBackout,
                                    addSong,
                                    songsReordered)
  val songSelect = new OrderableSelectionList[Song](songCallbacks,
                                                     Selector.renderSongItem,
                                                     Selector.renderAddButton,
                                                     componentBGColor)
  songSelect.setPreferredSize(new Dimension(250, size.height - 4))
  songSelect.setBackground(componentBGColor)

  val phraseCallbacks = new ListCallbacks(phraseClicked,
                                          phraseBackout,
                                          addPhrase,
                                          phrasesReordered)
  val phraseSelect = new OrderableSelectionList[Phrase](phraseCallbacks,
                                                         Selector.renderPhraseItem,
                                                         Selector.renderAddButton,
                                                         componentBGColor)
  phraseSelect.setPreferredSize(new Dimension(250, size.height - 4))
  phraseSelect.setBackground(componentBGColor)

  val selector = new Selector(songSelect, phraseSelect)
  selector.setOpaque(false)

  private val editorWidth = size.width - songSelect.getPreferredSize.width - phraseSelect.getPreferredSize.width - 12

  val songEditor = new SongEditor(onSongUpdated,
                                   onSongDeleted,
                                   new Dimension(editorWidth, 40), componentBGColor)
  val phraseEditor = new PhraseEditor(save,
                                       onPhraseUpdated,
                                       onDupPhrase,
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
    withSetList { setList => setList.setName(name) }
  }

  def songClicked(song: Song): Unit = {
    seismic.setCurrentSong(song)
  }

  def addSong(): Unit = {
    withSetList { setList =>
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

  def onSongDeleted(songToDelete: Song): Unit = {
    withSetList { setList =>
      val index = setList.songs.indexOf(songToDelete)
      setList.removeSong(songToDelete)
      save()

      updateSetList(setList)
      val songToSelect = if (index < 1) setList.songs(0) else setList.songs(index - 1)
      val phrase = songToSelect.getPhrases.head
      seismic.setCurrentSong(songToSelect)
      seismic.setCurrentPhrase(phrase)

      indicateSelectedSong(songToSelect)
      indicateSelectedPhrase(phrase)
    }
  }

  def songsReordered(songs: Seq[Song]): Unit = {
    withSetList { setList =>
      setList.updateSongs(songs)
      seismic.save()
    }
  }

  def addPhrase(): Unit = {
    withCurrentSong { song =>
      val newPhrase = song.addPhrase()
      phraseAdded(newPhrase)
    }
  }

  def onDupPhrase(phrase: Phrase): Unit = {
    withCurrentSong { song =>
      val newPhrase = song.dupPhrase(phrase)
      phraseAdded(newPhrase)
    }
  }

  def onPhraseDeleted(phrase: Phrase): Unit = {
    withCurrentSong { song =>
      val phraseIdx = song.getPhrases.indexOf(phrase)

      song.removePhrase(phrase)
      save()

      val phraseToSelect = if (phraseIdx < 1) song.getPhrases(0) else song.getPhrases(phraseIdx - 1)
      seismic.setCurrentPhrase(phraseToSelect)
      phraseSelect.setItems(song.getPhrases)
      indicateSelectedPhrase(phrase)
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
    withCurrentSong { song =>
      song.updatePhrases(phrases)
      seismic.save()
    }
  }

  private def phraseAdded(newPhrase: Phrase): Unit = {
    save()
    phraseSelect.addItem(newPhrase)
    seismic.setCurrentPhrase(newPhrase)
    phraseEditor.grabFocus()
  }

  private def indicateSelectedSong(song: Song): Unit = {
    val phrases = song.getPhrases
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

  private def setSetList(setList: SetList): Unit = {
    val song = setList.songs.head
    val phrase = song.getPhrases.head

    updateSetList(setList)

    indicateSelectedSong(song)
    indicateSelectedPhrase(phrase)
  }

  private def updateSetList(setList: SetList): Unit = {
    nameField.setText(setList.name)
    songSelect.setItems(setList.songs)
  }

  private def withSetList[T](f: (SetList) => T) = {
    seismic.setListOpt.foreach { setList => f(setList) }
  }

  private def withCurrentSong[T](f: (Song) => T) = {
    seismic.currentSongOpt.foreach { song => f(song) }
  }
}


class Editor(songEditor: SongEditor,
             phraseEditor: PhraseEditor) extends JPanel {

  setPreferredSize(new Dimension(Sizing.fitWidth(songEditor), Sizing.fitHeight(songEditor, phraseEditor)))

  position(songEditor).atOrigin().in(this)
  position(phraseEditor).below(songEditor).withMargin(4).in(this)
}

