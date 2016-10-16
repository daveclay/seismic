package com.seismic.ui

import java.awt.event.KeyListener
import java.awt.{Color, Dimension, GridBagLayout, Insets}
import javax.swing.{BorderFactory, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.ui.utils._
import com.seismic.ui.utils.draglist.{ListCallbacks, OrderableSelectionList}
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.{Phrase, Seismic, SetList, Song}

class SetlistUI(seismic: Seismic,
                callbacks: SeismicSerialCallbacks,
                size: Dimension,
                backgroundColor: Color,
                componentBGColor: Color) extends JPanel {

  setPreferredSize(size)
  setMinimumSize(size)

  seismic.onPhraseChange { (phrase: Phrase) =>
    indicateSelectedSong(phrase.song)
    indicateSelectedPhrase(phrase)
  }

  val nameField = new LabeledTextField("Set List", 40, onSetListNameChange)
  val namePanel = new JPanel
  private val nameSize = new Dimension(300, 30)
  namePanel.setPreferredSize(nameSize)
  namePanel.setMinimumSize(nameSize)
  namePanel.setBackground(componentBGColor)
  position(nameField).at(4, 4).in(namePanel)
  SwingComponents.addBorder(namePanel)

  val songCallbacks = ListCallbacks(songClicked,
                                    songBackout,
                                    addSong,
                                    songsReordered)

  val songSelect = new OrderableSelectionList[Song](songCallbacks,
                                                     Selector.renderSongItem,
                                                     Selector.renderAddButton,
                                                     componentBGColor)
  private val selectSize = new Dimension(250, 200)
  songSelect.setPreferredSize(selectSize)
  songSelect.setMinimumSize(selectSize)
  songSelect.setBackground(componentBGColor)

  val phraseCallbacks = new ListCallbacks(phraseClicked,
                                          phraseBackout,
                                          addPhrase,
                                          phrasesReordered)
  val phraseSelect = new OrderableSelectionList[Phrase](phraseCallbacks,
                                                         Selector.renderPhraseItem,
                                                         Selector.renderAddButton,
                                                         componentBGColor)
  phraseSelect.setPreferredSize(selectSize)
  phraseSelect.setMinimumSize(selectSize)
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

  val helper = new GridBagLayoutHelper(this)

  helper.position(namePanel).withPadding(4).atOrigin().colspan(2).fillHorizontal().alignLeft().inParent()
  helper.position(selector).below(namePanel).fillVertical().alignLeft().weightY(.5f).inParent()
  helper.position(editor).nextTo(selector).fill().alignLeft().weightY(1).weightX(.6f).alignLeft().inParent()

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

  private val editorSize = new Dimension(Sizing.fitWidth(songEditor),
                                          Sizing.fitHeight(songEditor, phraseEditor))
  setPreferredSize(editorSize)
  setMinimumSize(editorSize)

  val helper = new GridBagLayoutHelper(this)
  helper.position(songEditor).atOrigin().withPadding(new Insets(0, 0, 0, 4)).fillHorizontal().weightX(1).alignLeft().inParent()
  helper.position(phraseEditor).below(songEditor).withPadding(new Insets(0, 0, 0, 4)).fill().weightX(1).weightY(1).alignLeft().inParent()
}

