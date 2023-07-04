package matejsaric32.android.mytodolist.models

import android.os.Parcel
import android.os.Parcelable

data class Card(
    val name: String? = "",
    val createdBy: String? = "",
    val assignedTo: ArrayList<String> = ArrayList(),
    val dueDate: String? = "",
    val colorLabel: String? = "",
    var checkList: ArrayList<CheckItem> = ArrayList(),
    var attachmentList: ArrayList<String> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(CheckItem.CREATOR)!!,
        parcel.createStringArrayList()!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(createdBy)
        parcel.writeStringList(assignedTo)
        parcel.writeString(dueDate)
        parcel.writeString(colorLabel)
        parcel.writeTypedList(checkList)
        parcel.writeStringList(attachmentList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}