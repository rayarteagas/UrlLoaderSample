package com.mukoapps.urlloader

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LoaderTest {
    lateinit var subject: DownloadableString

    /**
     * Before the test runs initialize subject
     */
    @Before
    fun setup() {
        subject = DownloadableString("http://pastebin.com/raw/wgkJgazE")
    }

    @Test
    fun whenMainViewModelClicked_showSnackbar() {
        runBlocking {
            var st:String? = null
            subject.load { s, throwable ->
                st=s
            }
            assert(st!=null )
            }
        }
    }