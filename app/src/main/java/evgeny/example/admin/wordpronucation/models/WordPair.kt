package evgeny.example.admin.wordpronucation.models

class WordPair(private val word: String, private val tWord: String) {
    var originalWord: String = word
    var translatedWord: String = tWord
}