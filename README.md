# Multi-Modal SJSU Campus Tour App

**Joon Hyuk Song**

### **Project Description**
This project seeks to provide students and parents with an accessible and comprehensive resource to learn more about San Jose State University (SJSU), catering mainly to those who missed the in-person orientation or experienced challenges with campus tours. Although Google Maps can identify building locations, it often lacks detailed contextual information, leaving many reliant on outdated paper maps that are incomplete and insufficient. Additionally, the lack of integration between the map's data and users' views requires repeated searches for individual building details, making the process inefficient and frustrating. This project aims to deliver a streamlined, user-friendly solution for navigating and understanding the SJSU campus by addressing these gaps.

This project will assist admitted students and high school students taking campus tours. Our app is **multi-modal**, incorporating diverse input/output mechanisms such as **camera, microphone, and speaker**.

## **Project Goals**

### **Backend Goals**
- Fine-tune YOLO v5 that can detect **SJSU-specific objects** and generate object detection data.
- Distill **ChatGPT-SJSU Buddy** and use **Retrieval-Augmented Generation (RAG)** with a pre-trained **Small Language Model (SLM)**.
- The SLM should generate answers for user queries and explain detected objects.

### **Frontend Goals**
- Capture **real-time images** from the camera and predict labels and bounding boxes using fine-tuned YOLO v5.
- Receive the answer from the Python server and generate a **voice-supported answer**.
- Accept user input through **microphone**.

---

## **Features**
- **Real-time object detection**: The app can detect SJSU-specific objects using the camera and provide real-time results.
- **Voice feedback**: Users can interact with the app using voice commands 
- **Multi-modal interaction**: The app supports input via camera, microphone, speakers, and text, enhancing user accessibility.
- **Chatbot assistance**: The RAG-based SLM provides detailed information about SJSU to the users.


### **Backend Requirements**
- Python 
- Ollama, Langchain
- Flask (for API development)

### **Frontend (Android) Requirements**
- Android Studio
- Java (for Android development)
- Camera2 API
- Android Speech-to-Text API
- Android Text-to-Speech

---

## **Installation & Setup**

### 1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Joonhyuk-Song/SJSU_Campus_Tour.git
