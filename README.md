# 🎬Movie Mood Matcher [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/anirban94chakraborty/movie-mood-matcher)

**Movie Mood Matcher** is a stateless, cloud-native microservice application. It utilises Google's Agent Development Kit (ADK) and Gemini on Vertex AI to seamlessly translate unstructured human emotion into deterministic, system-routed movie recommendations.



## 🎯Problem Statement & Solution

Build and deploy a single AI agent using ADK and Gemini that performs **one clearly defined task (Routing a request to a fixed response logic)** via an HTTP endpoint.

This application solves that by creating a strict NLP classification boundary: 

1. **The AI Layer:** Uses Gemini 2.5 Flash to understand the nuance of a user's text input and strictly classify it into predefined buckets (`ROAD_TRIP`, `LAUGH_OUT_LOUD`, `ADRENALINE`, or `OTHER`). 
2. **The Deterministic Layer:** Uses standard application logic (a Java `switch` statement) to route that classification to a fixed, pre-approved response, eliminating the risk of AI hallucinations.



## 🏗️Architecture & Tech Stack

* **Language/Framework:** Java 17, Spring Boot (REST API) 

* **AI Integration:** Google Agent Development Kit (ADK), Vertex AI (Gemini 2.5 Flash) 
* **Deployment:** Containerised via Docker, hosted serverless on Google Cloud Run 
* **Session Management:** Stateless HTTP endpoint wrapping isolated, per-request ADK `InMemoryRunner` sessions. 



## 🚀API Reference

* **Endpoint:** `POST /api/match-mood` 

* **Content-Type:** `application/json`

* **Request Body**: 

  Accepts free-form text describing how the user is currently feeling.

  ```json
  {
    "moodText": "Just me, the open highway, and my thoughts. I need a movie that feels like a warm breeze."
  }
  ```

* **Response Body**:

  Returns the system-classified category and the fixed recommendation.

  ```json
  {
    "detected_mood_category": "ROAD_TRIP",
    "recommendation": "You should watch 'Karwaan'. It's the perfect thoughtful, soulful journey to match your vibe."
  }
  ```

  

  > **Graceful Degradation (Edge Cases)**
  >
  > If the AI detects an emotion that does not fit the predefined routing buckets (e.g., sadness, romance, sci-fi), it is instructed to fallback to an `OTHER` category, ensuring the application always returns a safe, predictable default response.



## 💻Local Development Setup

1. Clone the repository and navigate to the directory:

   ```bash
   git clone https://github.com/anirban94chakraborty/movie-mood-matcher.git
   cd movie-mood-matcher
   ```

   

2. Authenticate with Google Cloud (for Vertex AI):

   ```bash
   gcloud auth application-default login
   ```

   

3. Set required environment variables:

   ```bash
   export GOOGLE_GENAI_USE_VERTEXAI=TRUE
   export GOOGLE_CLOUD_PROJECT="<your-project-id>"
   export GOOGLE_CLOUD_LOCATION="us-central1"
   ```

   

4. Run the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080/api/match-mood`.



## ☁️Deployment

This application is fully containerised and designed for stateless execution on Google Cloud Run.

**Build and Deploy command:**

```bash
gcloud run deploy movie-mood-matcher \
  --source . \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8080 \
  --set-env-vars GOOGLE_GENAI_USE_VERTEXAI=TRUE,GOOGLE_CLOUD_PROJECT=<your-project-id>,GOOGLE_CLOUD_LOCATION=us-central1
```







