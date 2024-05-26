package com.example.futlives.ui.theme

import com.example.futlives.League
import com.example.futlives.LiveScoreResponse
import com.example.futlives.Player
import com.example.futlives.Team
import retrofit2.http.GET
import retrofit2.http.Path

interface SoccerApi {
    @GET("v4/soccer/scores/json/Competitions?key=13f51eb0b3b64c3f9049bc42450979e7")
    suspend fun getLeagues(): List<League>

    @GET("v4/soccer/scores/json/Teams/{competitionId}?key=13f51eb0b3b64c3f9049bc42450979e7")
    suspend fun getTeams(@Path("competitionId") competitionId: String): List<Team>

    @GET("v4/soccer/scores/json/PlayersByTeam/{competitionId}/{teamId}?key=13f51eb0b3b64c3f9049bc42450979e7")
    suspend fun getPlayers(@Path("competitionId") competitionId: String, @Path("teamId") teamId: String): List<Player>

    @GET("football/?met=Livescore&APIkey=2ef27fb46529fe0bff721783fd24ae774d05acd5fbf1a5d32578d960c795bcb4")
    suspend fun getLiveScores(): LiveScoreResponse

}
