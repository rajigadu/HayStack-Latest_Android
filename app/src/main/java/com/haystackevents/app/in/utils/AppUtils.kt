package com.haystackevents.app.`in`.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.network.ContactInfo

object AppUtils {

    fun getContactInfo(context: Context, uri: Uri?): ContactInfo? {
        val contactId = getContactId(context, uri)
        var contactData: ContactInfo? = null
        val dataContentUri: Uri = ContactsContract.Data.CONTENT_URI
        val queryColumnList: MutableList<String> = ArrayList()
        queryColumnList.add(ContactsContract.Data.CONTACT_ID)
        queryColumnList.add(ContactsContract.Data.MIMETYPE)
        queryColumnList.add(ContactsContract.Data.DATA1)
        queryColumnList.add(ContactsContract.Data.DATA2)
        queryColumnList.add(ContactsContract.Data.DATA3)
        queryColumnList.add(ContactsContract.Data.DATA4)
        queryColumnList.add(ContactsContract.Data.DATA5)
        queryColumnList.add(ContactsContract.Data.DATA6)
        queryColumnList.add(ContactsContract.Data.DATA7)
        queryColumnList.add(ContactsContract.Data.DATA8)
        queryColumnList.add(ContactsContract.Data.DATA9)
        queryColumnList.add(ContactsContract.Data.DATA10)
        queryColumnList.add(ContactsContract.Data.DATA11)
        queryColumnList.add(ContactsContract.Data.DATA12)
        queryColumnList.add(ContactsContract.Data.DATA13)
        queryColumnList.add(ContactsContract.Data.DATA14)
        queryColumnList.add(ContactsContract.Data.DATA15)
        val queryColumnArr = queryColumnList.toTypedArray()
        val whereClauseBuf = StringBuffer()
        whereClauseBuf.append(ContactsContract.Data.CONTACT_ID)
        whereClauseBuf.append("=")
        whereClauseBuf.append(contactId)
        val cursor = context.contentResolver.query(
            dataContentUri,
            queryColumnArr,
            whereClauseBuf.toString(),
            null,
            null
        )
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            contactData = ContactInfo()
            do {
                when (cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE))) {
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        val emailAddress =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        val emailType =
                            cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                        if (ContactsContract.CommonDataKinds.Email.TYPE_HOME == emailType) {
                            contactData.homeEmailId = emailAddress
                        } else if (ContactsContract.CommonDataKinds.Email.TYPE_WORK == emailType) {
                            contactData.workEmailId = emailAddress
                        }
                    }
                    ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE -> {
                        val nickName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME))
                        contactData.nickName = nickName
                    }
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        val phoneNumber =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        when (cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))) {
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> {
                                contactData.homePhoneNumber = phoneNumber
                            }
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> {
                                contactData.workPhoneNumber = phoneNumber
                            }
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> {
                                contactData.mobilePhoneNumber = phoneNumber
                            }
                        }
                    }
                    ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE -> {
                        val address =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS))
                        val addressTypeInt =
                            cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.TYPE))
                        // TODO: 10/11/20 Address need to specify the type
                    }
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {

                        val displayName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME))

                        val firstName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                        val middleName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                        val lastName =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                        contactData.displayName = displayName
                        contactData.firstName = firstName
                        contactData.middleName = middleName
                        contactData.lastName = lastName
                    }
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> {
                        val country =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY))
                        val city =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY))
                        val region =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION))
                        val street =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET))
                        val postcode =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE))
                        val postType =
                            cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
                        contactData.country = country
                        contactData.city = city
                        contactData.state = region
                        contactData.address2 = street
                    }
                    ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE -> {
                        val relation =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Relation.NAME))
                        contactData.relation = relation
                    }
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
        return contactData
    }

    private fun getContactId(context: Context, uri: Uri?): String? {
        var contactID: String? = null
        uri?.let {
            val cursorID: Cursor? = context.contentResolver.query(
                it, arrayOf(ContactsContract.Contacts._ID),
                null, null, null
            )
            if (cursorID != null && cursorID.moveToFirst()) {
                contactID =
                    cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID))
            }
            cursorID?.close()
        }
        return contactID
    }

    fun AppCompatActivity.navigate(
        fragment: Fragment,
        addToBackStack: Boolean = true,
        animation: Boolean = true,
        backStackName: String = "",
    ) {

        val rootView: ViewGroup = findViewById(android.R.id.content)
        val container = rootView.findViewById<FrameLayout>(R.id.container)
            ?: throw Throwable("Activity FrameLayout id needs to be \"container\"")
        if (animation) {
            add(fragment, container.id, addToBackStack, backStackName)
        }
    }
}

fun AppCompatActivity.add(
    fragment: Fragment,
    @IdRes container: Int,
    addToBackStack: Boolean = false,
    backStackName: String = ""
) {
    supportFragmentManager.transact {
        add(container, fragment, backStackName)
        if (addToBackStack) addToBackStack(backStackName)
    }
}

private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commitAllowingStateLoss()
}