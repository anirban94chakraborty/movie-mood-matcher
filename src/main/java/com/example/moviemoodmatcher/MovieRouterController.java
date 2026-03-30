package com.example.moviemoodmatcher;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

// Official ADK Imports based on the documentation
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;

@RestController
@RequestMapping("/api")
public class MovieRouterController {

    private final BaseAgent agent;
    private final InMemoryRunner runner;

    public MovieRouterController() {
        // 1. Initialize the Agent using the builder pattern from the docs
        this.agent = LlmAgent.builder()
            .name("movie-mood-classifier")
            .description("Classifies user mood into fixed movie categories")
            .instruction("You are a strict text classifier. Read the user's mood and classify it into one of these three categories: ROAD_TRIP, LAUGH_OUT_LOUD, ADRENALINE. If the mood does not clearly fit any of those three, respond with OTHER. Respond with ONLY the exact category name and absolutely nothing else.")
            .model("gemini-2.5-flash") 
            .build();
            
        // 2. Initialize the runner to execute the agent
        this.runner = new InMemoryRunner(this.agent);
    }

    @PostMapping("/match-mood")
    public ResponseEntity<Map<String, String>> matchMovie(@RequestBody MoodRequest request) {
        
        String category = "ROAD_TRIP"; // Default fallback

        try {
            // 3. Set up the Run Configuration and Session
            RunConfig runConfig = RunConfig.builder().build();
            
            // Create a unique session for each request so concurrent users don't mix state
            String sessionId = "user-" + UUID.randomUUID().toString();
            Session session = runner.sessionService()
                                    .createSession(runner.appName(), sessionId)
                                    .blockingGet();
            
            // 4. Format the user's input
            Content userMsg = Content.fromParts(Part.fromText("User Mood: " + request.getMoodText()));
            
            // 5. Execute the agent asynchronously
            Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg, runConfig);
            
            // 6. Extract the final response text
            StringBuilder responseBuilder = new StringBuilder();
            events.blockingForEach(event -> {
                if (event.finalResponse()) {
                    responseBuilder.append(event.stringifyContent());
                }
            });
            
            // Clean up the response (in case Gemini adds markdown ticks or newlines)
            category = responseBuilder.toString().replace("`", "").trim(); 
            
        } catch (Exception e) {
            System.err.println("ADK Inference Failed: " + e.getMessage());
        }

        // 7. Route to fixed logic based on the AI's classification
        String movieRecommendation;
        switch (category) {
            case "ROAD_TRIP":
                movieRecommendation = "You should watch 'Karwaan' (starring Irrfan Khan and Dulquer Salmaan). It's the perfect thoughtful, soulful journey to match your vibe.";
                break;
            case "LAUGH_OUT_LOUD":
                movieRecommendation = "You should watch 'Hera Pheri'. Guaranteed laughs.";
                break;
            case "ADRENALINE":
                movieRecommendation = "Check out 'Mad Max: Fury Road' for non-stop action.";
                break;
            default:
                movieRecommendation = "Let's stick to a classic: 'The Shawshank Redemption'.";
        }

        return ResponseEntity.ok(Map.of(
            "detected_mood_category", category,
            "recommendation", movieRecommendation
        ));
    }
}

class MoodRequest {
    private String moodText;
    public String getMoodText() { return moodText; }
    public void setMoodText(String moodText) { this.moodText = moodText; }
}