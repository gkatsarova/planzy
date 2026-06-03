package com.planzy.app.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FollowRepositoryIntegrationTest : BaseRepositoryIntegrationTest() {

    private lateinit var followRepository: FollowRepositoryImpl

    companion object {
        const val DUMMY_UUID = "00000000-0000-0000-0000-000000000000"
    }

    @Before
    fun setup() {
        followRepository = FollowRepositoryImpl(
            resourceProvider = getResourceProvider()
        )
    }

    @Test
    fun getFollowersWithDummyUserIdReturnsEmptyListSuccessfully() = runTest {
        val result = followRepository.getFollowers(DUMMY_UUID)
        assertTrue(result.isSuccess)
        val followers = result.getOrNull()
        assertNotNull(followers)
        assertTrue(followers!!.isEmpty())
    }

    @Test
    fun getFollowersResultIsNotNull() = runTest {
        val result = followRepository.getFollowers(DUMMY_UUID)
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun getFollowingWithDummyUserIdReturnsEmptyListSuccessfully() = runTest {
        val result = followRepository.getFollowing(DUMMY_UUID)
        assertTrue(result.isSuccess)
        val following = result.getOrNull()
        assertNotNull(following)
        assertTrue(following!!.isEmpty())
    }

    @Test
    fun followUserWithoutAuthReturnsFailure() = runTest {
        val result = followRepository.followUser(DUMMY_UUID)
        assertTrue(result.isFailure)
        assertFalse(result.exceptionOrNull()?.message.isNullOrEmpty())
    }

    @Test
    fun unfollowUserWithoutAuthReturnsFailure() = runTest {
        val result = followRepository.unfollowUser(DUMMY_UUID)
        assertTrue(result.isFailure)
    }

    @Test
    fun isFollowingWithoutAuthReturnsFailure() = runTest {
        val result = followRepository.isFollowing(DUMMY_UUID)
        assertTrue(result.isFailure)
    }

    @Test
    fun getFollowStatsWithoutAuthReturnsFailureGracefully() = runTest {
        val result = followRepository.getFollowStats(DUMMY_UUID)
        assertTrue(result.isFailure)
    }
}
