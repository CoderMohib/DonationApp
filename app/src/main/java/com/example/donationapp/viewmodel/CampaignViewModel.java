package com.example.donationapp.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.donationapp.model.Campaign;
import com.example.donationapp.util.FirebaseHelper;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for campaign operations
 * Handles CRUD operations and real-time updates for campaigns
 */
public class CampaignViewModel extends AndroidViewModel {
    private static final String TAG = "CampaignViewModel";
    private FirebaseHelper firebaseHelper;
    private ListenerRegistration campaignsListener;
    
    private MutableLiveData<List<Campaign>> campaigns = new MutableLiveData<>();
    private MutableLiveData<Campaign> selectedCampaign = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isSearching = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private List<Campaign> allCampaigns = new ArrayList<>(); // Store all campaigns for filtering
    private String currentSearchQuery = "";
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public CampaignViewModel(@NonNull Application application) {
        super(application);
        firebaseHelper = FirebaseHelper.getInstance();
        campaigns.setValue(new ArrayList<>());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove listener when ViewModel is cleared
        if (campaignsListener != null) {
            campaignsListener.remove();
        }
    }

    /**
     * Start listening to campaigns in real-time
     */
    public void startListeningToCampaigns() {
        if (campaignsListener != null) {
            return; // Already listening
        }

        isLoading.setValue(true);
        
        campaignsListener = firebaseHelper.getFirestore()
                .collection("campaigns")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to campaigns", error);
                        errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(error));
                        isLoading.setValue(false);
                        return;
                    }

                    if (snapshot != null) {
                        List<Campaign> campaignList = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            campaign.setId(doc.getId());
                            campaignList.add(campaign);
                        }
                        allCampaigns = campaignList;
                        // Apply current search filter if any
                        applySearchFilter();
                        isLoading.setValue(false);
                    }
                });
    }

    /**
     * Stop listening to campaigns
     */
    public void stopListeningToCampaigns() {
        if (campaignsListener != null) {
            campaignsListener.remove();
            campaignsListener = null;
        }
    }

    /**
     * Load all campaigns once (non-real-time)
     */
    public void loadCampaigns() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.getAllCampaigns(
                querySnapshot -> {
                    List<Campaign> campaignList = new ArrayList<>();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                            Campaign campaign = doc.toObject(Campaign.class);
                            campaign.setId(doc.getId());
                            campaignList.add(campaign);
                        }
                    }
                    allCampaigns = campaignList;
                    // Apply current search filter if any
                    applySearchFilter();
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error loading campaigns", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Load campaign by ID
     */
    public void loadCampaign(String campaignId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.getCampaign(campaignId,
                campaign -> {
                    selectedCampaign.setValue(campaign);
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error loading campaign", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Create new campaign
     */
    public void createCampaign(Campaign campaign) {
        if (campaign == null) {
            errorMessage.setValue("Campaign data is invalid");
            return;
        }
        
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.createCampaign(campaign,
                campaignId -> {
                    Log.d(TAG, "Campaign created: " + campaignId);
                    isLoading.setValue(false);
                    // Clear error on success
                    errorMessage.setValue(null);
                    // Campaigns list will update automatically via listener
                },
                exception -> {
                    Log.e(TAG, "Error creating campaign", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Update campaign
     */
    public void updateCampaign(String campaignId, Map<String, Object> updates) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        firebaseHelper.updateCampaign(campaignId, updates,
                aVoid -> {
                    Log.d(TAG, "Campaign updated");
                    isLoading.setValue(false);
                    // Refresh selected campaign if it's the one being updated
                    if (selectedCampaign.getValue() != null && 
                        campaignId.equals(selectedCampaign.getValue().getId())) {
                        loadCampaign(campaignId);
                    }
                },
                exception -> {
                    Log.e(TAG, "Error updating campaign", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    /**
     * Delete campaign
     */
    public void deleteCampaign(String campaignId, String imageUrl) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        
        // TODO: Uncomment when Firebase Storage is enabled
        // Original Firebase Storage image deletion code (commented out temporarily)
        // // Delete image first, then campaign document
        // firebaseHelper.deleteImage(imageUrl,
        //         aVoid -> {
        //             // Image deleted, now delete campaign
        //             firebaseHelper.deleteCampaign(campaignId,
        //                     aVoid1 -> {
        //                         Log.d(TAG, "Campaign deleted");
        //                         isLoading.setValue(false);
        //                     },
        //                     exception -> {
        //                         Log.e(TAG, "Error deleting campaign", exception);
        //                         errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
        //                         isLoading.setValue(false);
        //                     });
        //         },
        //         exception -> {
        //             // Even if image deletion fails, try to delete campaign
        //             Log.w(TAG, "Error deleting image, continuing with campaign deletion", exception);
        //             firebaseHelper.deleteCampaign(campaignId,
        //                     aVoid -> {
        //                         Log.d(TAG, "Campaign deleted (image deletion failed)");
        //                         isLoading.setValue(false);
        //                     },
        //                     exception1 -> {
        //                         Log.e(TAG, "Error deleting campaign", exception1);
        //                         errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception1));
        //                         isLoading.setValue(false);
        //                     });
        //         });

        // Temporary fallback: Delete campaign without deleting image (works without Storage)
        firebaseHelper.deleteCampaign(campaignId,
                aVoid -> {
                    Log.d(TAG, "Campaign deleted (image deletion skipped - Storage disabled)");
                    isLoading.setValue(false);
                },
                exception -> {
                    Log.e(TAG, "Error deleting campaign", exception);
                    errorMessage.setValue(firebaseHelper.getFirestoreErrorMessage(exception));
                    isLoading.setValue(false);
                });
    }

    // Getters for LiveData
    public LiveData<List<Campaign>> getCampaigns() {
        return campaigns;
    }

    public LiveData<Campaign> getSelectedCampaign() {
        return selectedCampaign;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsSearching() {
        return isSearching;
    }

    /**
     * Search campaigns by query string
     * Filters campaigns by title and description
     */
    public void searchCampaigns(String query) {
        if (query == null) {
            query = "";
        }
        String newQuery = query.trim().toLowerCase();
        
        // Cancel previous search if any
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        
        // If query is empty, clear search immediately
        if (newQuery.isEmpty()) {
            currentSearchQuery = "";
            applySearchFilter();
            isSearching.setValue(false);
            return;
        }
        
        // Show loader if query is different from current
        if (!newQuery.equals(currentSearchQuery)) {
            isSearching.setValue(true);
        }
        
        currentSearchQuery = newQuery;
        
        // Debounce search to show loader briefly
        searchRunnable = () -> {
            applySearchFilter();
            isSearching.setValue(false);
        };
        
        // Post with small delay to ensure loader is visible
        searchHandler.postDelayed(searchRunnable, 150);
    }

    /**
     * Apply search filter to campaigns
     */
    private void applySearchFilter() {
        if (currentSearchQuery.isEmpty()) {
            campaigns.setValue(allCampaigns);
            return;
        }

        List<Campaign> filteredCampaigns = new ArrayList<>();
        for (Campaign campaign : allCampaigns) {
            String title = campaign.getTitle() != null ? campaign.getTitle().toLowerCase() : "";
            String description = campaign.getDescription() != null ? campaign.getDescription().toLowerCase() : "";
            
            if (title.contains(currentSearchQuery) || description.contains(currentSearchQuery)) {
                filteredCampaigns.add(campaign);
            }
        }
        campaigns.setValue(filteredCampaigns);
    }

    /**
     * Clear search and show all campaigns
     */
    public void clearSearch() {
        currentSearchQuery = "";
        campaigns.setValue(allCampaigns);
        isSearching.setValue(false);
    }
}

