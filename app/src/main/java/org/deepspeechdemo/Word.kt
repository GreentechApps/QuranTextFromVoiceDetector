package org.deepspeechdemo

class Word {
    var index : Int
    var  text: String
    var  isDetected: Boolean

    constructor(index : Int,text: String, isDetected: Boolean) {
        this.index = index
        this.text = text
        this.isDetected = isDetected
    }
}