package app.example.heymama

class Token {
    private var token: String? = null

    constructor() {

    }

    constructor(token: String){
        this.token= token
    }

    fun getToken():String?{
        return token
    }

    fun setToken(token: String?)
    {
        this.token=token!!
    }

}