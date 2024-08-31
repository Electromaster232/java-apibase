package me.djelectro.javaflasklike.enums;

/**
 * Features that can be activated for a campaign.
 */
public enum CampaignFeatures {
  // If a campaign has access to the basic fundraising service
  FUNDRAISING,
  // Whether a campaign has access to Raisable Video Services
  VIDEO,

  // Whether a campaign can open the support portal. The campaign MUST have an ID specified in the database column
  @Deprecated
  SUPPORT,
  // Unused.
  TEST,
  // None required
  NONE
}
