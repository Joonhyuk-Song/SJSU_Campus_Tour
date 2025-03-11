# Multi-Modal SJSU Campus Tour App

Joon Hyuk Song

### **Project Description**
This project seeks to provide students and parents with an accessible and comprehensive resource to learn more about San Jose State University (SJSU), catering mainly to those who missed the in-person orientation or experienced challenges with campus tours. Although Google Maps can identify building locations, it often lacks detailed contextual information, leaving many reliant on outdated paper maps that are incomplete and insufficient. Additionally, the lack of integration between the map's data and users' views requires repeated searches for individual building details, making the process inefficient and frustrating. By addressing these gaps, this project aims to deliver a streamlined and user-friendly solution for navigating and understanding the SJSU campus.

This project will assist admitted students and high school students taking campus tours. Our app is **multi-modal**, incorporating diverse input/output mechanisms such as **camera, microphone, and speaker**.

## **Project Goals**
### **Backend Goals**
- Build a model that can detect **SJSU-specific objects** and generate object detection data.
- Distill **ChatGPT-SJSU Buddy** and use **Retrieval-Augmented Generation (RAG)** with a pre-trained **Large Language Model (LLM)**.
- The model should generate answers for user queries and provide explanations for detected objects.

### **Frontend Goals**
- Develop an app that captures **real-time images** from the camera, receives object detection results from the model, and displays the results.
- Generate a **voice-supported answer** when the user clicks on a detected object.
- Accept user input through **typing and microphone** and receive AI-generated answers.

## **Assigned Roles**
- **Chelsie:** Distill **ChatGPT-SJSU Buddy** and utilize **Retrieval-Augmented Generation (RAG)** with an LLM.
- **Connor:** Build a model to detect **SJSU-specific objects** and generate object detection data.
- **Jason:** Program in **Kotlin** to capture **real-time images** from the camera and handle **text/microphone input**. (**Input**)
- **Joon:** Receive **object detection results**, display the results in the app, and generate **voice-supported answers** based on the AI responses. (**Output**)

---
## **Technical Plan**
### **Backend Plan**
#### **1. Build a Model to Detect SJSU-Specific Objects**
- **Data Collection:** Gather images of **SJSU buildings and objects**. Each teammate collects images of two different objects.
- **Data Modification:** Label the dataset using the **Florence-2 Large model** and convert the JSON file into a **YOLO-compatible TXT file**.
- **Model Training:** Fine-tune **YOLO** with the labeled dataset.
- **Model Evaluation:** Test the model on a validation set and with new images.

#### **2. Distill ChatGPT-SJSU Buddy using RAG**
- **Document Creation:** Ask **ChatGPT-SJSU Buddy** questions about campus objects and compile the information into a **PDF document**.
- **Document Retrieval:** Build a **retrieval system** to fetch relevant documents based on user queries.
- **Model Integration:** Use **Llama or another LLM** to generate more accurate responses.
- **API Development:** Develop an **API endpoint** that processes user queries, integrates RAG, and generates answers.

### **Frontend Plan (Android)**
#### **1. Real-Time Object Detection and Multi-Modal Input**
- **Camera Integration:** Capture real-time images using **CameraX/Camera2 API**.
- **Text Input:** Implement a text field for users to **type questions**.
- **Voice Input:** Integrate **Android Speech-to-Text API** to handle spoken queries.

#### **2. Display Results and Generate Voice-Supported Answers**
- **Voice Output:** Generate voice-supported explanations using **Android Text-to-Speech (TTS)**.
- **Query Handling:** Send user questions (typed/spoken) to the backend API and display responses.
- **Object Detection Display:** Show detected objects on the captured images.

---
## **Tools and Technologies**
### **Backend:**
- **Python** (for model training & API development)
- **TensorFlow/PyTorch** (for object detection)
- **Hugging Face Transformers** (for LLM & RAG)
- **Flask/ngrok** (for API deployment)

### **Frontend (Android):**
- **Java/Kotlin** (for Android app development)
- **CameraX/Camera2 API** (for real-time image capture)
- **Android Speech-to-Text API** (for voice input)
- **Android Text-to-Speech** (for voice-supported answers)

---
## **Installation & Setup**
1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Joonhyuk-Song/SJSU_Campus_Tour.git
   ```
2. **Backend Setup:**
   - Install required Python dependencies:
     ```bash
     pip install -r requirements.txt
     ```
   - Run the API:
     ```bash
     python app.py
     ```
3. **Frontend Setup:**
   - Open the project in **Android Studio**.
   - Sync Gradle and run the app on an **Android device or emulator**.

---
## **Contributing**
We welcome contributions! If you'd like to contribute:
1. Fork the repository.
2. Create a new branch (`feature-branch`).
3. Make your changes and commit them.
4. Push your branch and create a **Pull Request (PR)**.

---
## **License**
This project is licensed under the **MIT License** © 2025 [Joonhyuk Song](https://github.com/Joonhyuk-Song).

---
### 🎓 *SJSU Campus Tour App - Helping students explore SJSU with AI-powered assistance!* 🚀

