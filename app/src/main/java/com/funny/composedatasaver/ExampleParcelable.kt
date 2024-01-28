package com.funny.data_saver

import android.os.Parcel
import android.os.Parcelable

class ExampleParcelable(val name: String?, val age: Int): Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(age)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString() = "I'm $name, my age is $age"


    companion object CREATOR : Parcelable.Creator<ExampleParcelable> {
        override fun createFromParcel(parcel: Parcel): ExampleParcelable {
            return ExampleParcelable(parcel)
        }

        override fun newArray(size: Int): Array<ExampleParcelable?> {
            return arrayOfNulls(size)
        }
    }

    fun copy(name: String? = this.name, age: Int = this.age) = ExampleParcelable(name, age)
}