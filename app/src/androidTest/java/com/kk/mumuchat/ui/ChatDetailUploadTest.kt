package com.kk.mumuchat.ui

import android.net.Uri
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kk.mumuchat.MainActivity
import com.kk.mumuchat.viewmodel.ChatViewModel
import com.kk.mumuchat.viewmodel.ImageUploadStatus
import com.kk.mumuchat.viewmodel.ImageUploadUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatDetailUploadTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun openChatDetail() {
        composeRule.onNodeWithText("绫骨开发进度群").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun uploadProgress_updates_from_0_to_100() {
        val viewModel = ViewModelProvider(composeRule.activity).get(ChatViewModel::class.java)
        openChatDetail()
        val uri = Uri.parse("file:///test.png")
        composeRule.runOnIdle {
            viewModel.setUploadStateForTest(
                ImageUploadUiState(uri = uri, progress = 0, status = ImageUploadStatus.Uploading)
            )
        }
        composeRule.onNodeWithTag("uploadProgressText").assertTextEquals("0%")
        composeRule.runOnIdle {
            viewModel.setUploadStateForTest(
                ImageUploadUiState(uri = uri, progress = 50, status = ImageUploadStatus.Uploading)
            )
        }
        composeRule.onNodeWithTag("uploadProgressText").assertTextEquals("50%")
        composeRule.runOnIdle {
            viewModel.setUploadStateForTest(
                ImageUploadUiState(uri = uri, progress = 100, status = ImageUploadStatus.Uploading)
            )
        }
        composeRule.onNodeWithTag("uploadProgressText").assertTextEquals("100%")
    }

    @Test
    fun uploadFailed_shows_retry_and_can_restart() {
        val viewModel = ViewModelProvider(composeRule.activity).get(ChatViewModel::class.java)
        openChatDetail()
        val uri = Uri.parse("file:///test.png")
        composeRule.runOnIdle {
            viewModel.enqueueImageUpload(uri) {}
            viewModel.setUploadStateForTest(
                ImageUploadUiState(
                    uri = uri,
                    progress = 62,
                    status = ImageUploadStatus.Failed,
                    errorMessage = "上传失败，请重试"
                )
            )
        }
        composeRule.onNodeWithTag("uploadRetryButton").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("uploadProgressText").assertTextEquals("0%")
    }

    @Test
    fun uploadState_persists_after_recreate() {
        val viewModel = ViewModelProvider(composeRule.activity).get(ChatViewModel::class.java)
        openChatDetail()
        val uri = Uri.parse("file:///test.png")
        composeRule.runOnIdle {
            viewModel.setUploadStateForTest(
                ImageUploadUiState(uri = uri, progress = 66, status = ImageUploadStatus.Uploading)
            )
        }
        composeRule.onNodeWithTag("uploadProgressText").assertTextEquals("66%")
        composeRule.activityRule.scenario.recreate()
        openChatDetail()
        composeRule.onNodeWithTag("uploadProgressText").assertTextEquals("66%")
    }
}
