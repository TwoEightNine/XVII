/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twoeightnine.root.xvii.pin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.twoeightnine.root.xvii.R
import com.twoeightnine.root.xvii.lg.L
import com.twoeightnine.root.xvii.utils.showToast
import kotlinx.android.synthetic.main.fragment_camera2_basic.*
import java.io.File

class Camera2BasicFragment : Fragment(), SimpleCamera.ControllerDelegate {

    private lateinit var simpleCamera: SimpleCamera

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        simpleCamera = SimpleCamera(
                texture,
                File(requireContext().filesDir, "pic.jpg"),
                this
        )
        picture.setOnClickListener {
            simpleCamera.takePicture()
        }
    }

    override fun onResume() {
        super.onResume()
        simpleCamera.start()
    }

    override fun onPause() {
        simpleCamera.stop()
        super.onPause()
    }

    override fun onErrorOccurred(explanation: String, throwable: Throwable?) {
        showToast(context, explanation)
    }

    override fun onPictureTaken(file: File) {
        showToast(context, "taken: ${file.absolutePath}")
    }

    override fun onPreviewRatioUpdated(wToH: Float) {
        if (wToH == 0f) return
        
        L.tag("camera").log("wtoh = $wToH")

    }
}