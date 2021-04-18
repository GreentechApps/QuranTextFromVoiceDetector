package org.deepspeechdemo

class DetectableWord(var index: Int,var id: Int, var ayahId: Int, var suraId: Int, var text: String, var isDetected: Boolean) {

}

class DetectableAyah(var ayahId :Int, var suraId: Int,var index: Int, var text: String, var isDetected: Boolean,var words: ArrayList<DetectableWord>) {

}

class QuranAyah(var suraId: Int,var ayahId :Int, var text: String) {

}