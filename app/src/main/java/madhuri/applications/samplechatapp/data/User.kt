package madhuri.applications.samplechatapp.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val imageUri: String) : Parcelable{
    constructor(): this("", "", "")
}