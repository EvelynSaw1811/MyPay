# MyPay Seed Reference

This document records deterministic seed scenarios that are shared across services. The seed code runs under the `dev` Spring profile.

## Seed Users

| User | ID | Scenario |
| --- | --- | --- |
| Alice | `00000001-0000-0000-0000-000000000001` | Main MYR user, collection owner/admin, invitation sender |
| Bob | `00000002-0000-0000-0000-000000000002` | SGD user, collection owner, accepted-invitation inviter |
| Carol | `00000003-0000-0000-0000-000000000003` | Declined-invitation invitee |
| David | `00000004-0000-0000-0000-000000000004` | Collection owner, rejected-invitation inviter, pending invitee |
| Emma | `00000005-0000-0000-0000-000000000005` | Wallet, settlement, empty collection, top-up, and pending invitation coverage |
| Frank | `00000006-0000-0000-0000-000000000006` | Pending invitation invitee and single-member collection coverage |
| Grace | `00000007-0000-0000-0000-000000000007` | Inactive/quarantined preference coverage |

Source: `BACKEND/common-lib/src/main/java/com/mypay/common/constant/SeedUsers.java`

Removed seed accounts:

| Removed User | Previous ID | Reason | Replacement |
| --- | --- | --- | --- |
| Henry Low | `00000008-0000-0000-0000-000000000008` | Account without wallet added unnecessary broken-profile noise | Empty collection ownership moved to Emma |
| Ivy Teoh | `00000009-0000-0000-0000-000000000009` | Account without nickname added unnecessary display-name noise | Pending invite/top-up moved to Emma; single-member collection moved to Frank |

## Collection Invitation Seeds

| Seed ID | Reference ID | Collection | Inviter | Invitee | Role | Status | Scenario |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `INV_PENDING_FRANK` | `40000001-0000-0000-0000-000000000001` | Bali Trip 2025 | Alice | Frank | `MEMBER` | `PENDING` | Pending invite to an existing active trip |
| `INV_ACCEPTED_ALICE` | `40000002-0000-0000-0000-000000000002` | SG Weekend Getaway | Bob | Alice | `MEMBER` | `ACCEPTED` | Accepted invite; Alice is already a collection member |
| `INV_DECLINED_CAROL` | `40000003-0000-0000-0000-000000000003` | Holiday Dinner Party | David | Carol | `MEMBER` | `DECLINED` | Declined/rejected invite; Carol is not a member |
| `INV_PENDING_EMMA_C11` | `40000004-0000-0000-0000-000000000004` | Flat Share Planning | Alice | Emma | `MEMBER` | `PENDING` | Pending invite to an existing active wallet user |
| `INV_PENDING_DAVID_C11` | `40000005-0000-0000-0000-000000000005` | Flat Share Planning | Alice | David | `EDITOR` | `PENDING` | Pending invite with non-member role assignment |
| `INV_PENDING_BOB_C7` | `40000006-0000-0000-0000-000000000006` | Empty Planning Collection | Emma | Bob | `MEMBER` | `PENDING` | Additional pending invite to an empty/monthly collection |
| `INV_PENDING_CAROL_C7` | `40000007-0000-0000-0000-000000000007` | Empty Planning Collection | Emma | Carol | `EDITOR` | `PENDING` | Additional pending invite with editor role |
| `INV_PENDING_DAVID_C7` | `40000008-0000-0000-0000-000000000008` | Empty Planning Collection | Emma | David | `MEMBER` | `PENDING` | Additional pending invite for notification coverage |
| `INV_PENDING_FRANK_C7` | `40000009-0000-0000-0000-000000000009` | Empty Planning Collection | Emma | Frank | `MEMBER` | `PENDING` | Additional pending invite for repeated invitee coverage |
| `INV_PENDING_ALICE_C8` | `40000010-0000-0000-0000-000000000010` | Solo Coffee Run | Frank | Alice | `MEMBER` | `PENDING` | Additional pending invite to a single-member collection |
| `INV_PENDING_BOB_C8` | `40000011-0000-0000-0000-000000000011` | Solo Coffee Run | Frank | Bob | `EDITOR` | `PENDING` | Additional pending invite with editor role |
| `INV_PENDING_CAROL_C8` | `40000012-0000-0000-0000-000000000012` | Solo Coffee Run | Frank | Carol | `MEMBER` | `PENDING` | Additional pending invite for accept/reject dialog testing |
| `INV_PENDING_DAVID_C8` | `40000013-0000-0000-0000-000000000013` | Solo Coffee Run | Frank | David | `MEMBER` | `PENDING` | Additional pending invite for accept/reject dialog testing |
| `INV_PENDING_EMMA_C1` | `40000014-0000-0000-0000-000000000014` | Bali Trip 2025 | Alice | Emma | `MEMBER` | `PENDING` | Extra pending invite to an active trip |
| `INV_PENDING_FRANK_C2` | `40000015-0000-0000-0000-000000000015` | Office Lunch Pool | Bob | Frank | `MEMBER` | `PENDING` | Extra pending invite to an expense collection |
| `INV_PENDING_EMMA_C3` | `40000016-0000-0000-0000-000000000016` | SG Weekend Getaway | Bob | Emma | `MEMBER` | `PENDING` | Extra pending invite with SGD collection currency |
| `INV_PENDING_ALICE_C5` | `40000017-0000-0000-0000-000000000017` | Holiday Dinner Party | David | Alice | `MEMBER` | `PENDING` | Extra pending invite with USD collection currency |
| `INV_PENDING_EMMA_C8` | `40000018-0000-0000-0000-000000000018` | Solo Coffee Run | Frank | Emma | `MEMBER` | `PENDING` | Extra pending invite to a single-member collection |
| `INV_PENDING_FRANK_C9` | `40000019-0000-0000-0000-000000000019` | Quarterly Regional Product Operations And Settlement Reconciliation Workshop | Alice | Frank | `MEMBER` | `PENDING` | Extra pending invite with long collection name |
| `INV_DECLINED_DAVID_C2` | `40000020-0000-0000-0000-000000000020` | Office Lunch Pool | Bob | David | `MEMBER` | `DECLINED` | Extra rejected invitation from Bob |
| `INV_DECLINED_CAROL_C3` | `40000021-0000-0000-0000-000000000021` | SG Weekend Getaway | Bob | Carol | `MEMBER` | `DECLINED` | Extra rejected invitation with SGD collection currency |
| `INV_DECLINED_BOB_C5` | `40000022-0000-0000-0000-000000000022` | Holiday Dinner Party | David | Bob | `MEMBER` | `DECLINED` | Extra rejected invitation with USD collection currency |
| `INV_DECLINED_DAVID_C9` | `40000023-0000-0000-0000-000000000023` | Quarterly Regional Product Operations And Settlement Reconciliation Workshop | Alice | David | `EDITOR` | `DECLINED` | Extra rejected editor invitation with long collection name |
| `INV_DECLINED_EMMA_C10` | `40000024-0000-0000-0000-000000000024` | Budget Road Trip | Alice | Emma | `MEMBER` | `DECLINED` | Extra rejected invitation to all-settled collection |
| `INV_DECLINED_FRANK_C11` | `40000025-0000-0000-0000-000000000025` | Flat Share Planning | Alice | Frank | `MEMBER` | `DECLINED` | Extra rejected invitation to multi-invite collection |

Source: `BACKEND/collection-service/src/main/java/com/mypay/collection/seed/SeedDataInitializer.java`

## Closed Collection Seed Rule

Closed collections must not contain unsettled expense shares. `Archived Movie Night` is seeded with all movie-ticket shares settled, and the collection-service seeder backfills the existing `E10_CLOSED_MOVIE_TICKETS` shares to settled if an older dev database already contains the previous unsettled Frank row.

## Expense Split Seed Rules

Expense seed shares are expected to add up to the stored expense total for the effective split total. Payer self-shares are seeded as settled with no settled timestamp, because the payer's own division is cleared immediately and should be visible in the collection `Me` tab but not in settlement history.

`Catered Dinner` (`E8_TAXED_CATERING`) stores `318.00 MYR` as the expense total: six equal base shares of `50.00 MYR` plus `6%` simple tax produce six `53.00 MYR` post-tax shares.

The collection-service seeder also backfills older dev databases where `E8_TAXED_CATERING` still has the previous `300.00 MYR` total, and inserts the long-name workshop expense (`E12_LONG_NAME_SUPPLIES`) when the long-name collection already exists without that expense.

## Currency Seed Rules

Source: `BACKEND/currency-service/src/main/java/com/mypay/currency/config/DataInitializer.java`

The currency service seeds active `MYR`, `SGD`, and `USD`, plus inactive `JPY`, and deterministic exchange-rate rows for `MYR`, `SGD`, and `USD`. Exchange-rate rows are inserted by deterministic ID so restarted demo databases keep the expected stale/current rate coverage without crashing on generated-id JPA merge behavior.

## Invitation Notification Seeds

The notification seed mirrors every collection invitation so `/app/notifications` has viewable data for sent, pending, accepted, and declined/rejected invitation states.

| Invitation | Viewer | Notification Type | Read | Message Purpose |
| --- | --- | --- | --- | --- |
| `INV_PENDING_FRANK` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited Frank to Bali Trip 2025 |
| `INV_PENDING_FRANK` | Frank | `INVITATION_RECEIVED` | No | Frank sees a pending invitation from Alice |
| `INV_ACCEPTED_ALICE` | Bob | `INVITATION_SENT` | Yes | Bob sees that he invited Alice to SG Weekend Getaway |
| `INV_ACCEPTED_ALICE` | Alice | `INVITATION_RECEIVED` | Yes | Alice sees the original invitation from Bob before her accepted response |
| `INV_ACCEPTED_ALICE` | Alice | `INVITATION_ACCEPTED_CONFIRMATION` | Yes | Alice sees that she accepted Bob's invitation |
| `INV_ACCEPTED_ALICE` | Bob | `INVITATION_ACCEPTED` | No | Bob sees that Alice accepted his invitation |
| `INV_DECLINED_CAROL` | David | `INVITATION_SENT` | Yes | David sees that he invited Carol to Holiday Dinner Party |
| `INV_DECLINED_CAROL` | Carol | `INVITATION_RECEIVED` | Yes | Carol sees the original invitation from David before her rejected response |
| `INV_DECLINED_CAROL` | Carol | `INVITATION_DECLINED_CONFIRMATION` | Yes | Carol sees that she rejected David's invitation |
| `INV_DECLINED_CAROL` | David | `INVITATION_REJECTED` | No | David sees that Carol rejected the invitation |
| `INV_PENDING_EMMA_C11` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited Emma to Flat Share Planning |
| `INV_PENDING_EMMA_C11` | Emma | `INVITATION_RECEIVED` | No | Emma sees a pending invitation from Alice |
| `INV_PENDING_DAVID_C11` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited David as `EDITOR` |
| `INV_PENDING_DAVID_C11` | David | `INVITATION_RECEIVED` | No | David sees a pending `EDITOR` invitation from Alice |
| `INV_PENDING_BOB_C7` | Emma | `INVITATION_SENT` | Yes | Emma sees that she invited Bob to Empty Planning Collection |
| `INV_PENDING_BOB_C7` | Bob | `INVITATION_RECEIVED` | No | Bob sees a pending invitation from Emma |
| `INV_PENDING_CAROL_C7` | Emma | `INVITATION_SENT` | Yes | Emma sees that she invited Carol as `EDITOR` |
| `INV_PENDING_CAROL_C7` | Carol | `INVITATION_RECEIVED` | No | Carol sees a pending `EDITOR` invitation from Emma |
| `INV_PENDING_DAVID_C7` | Emma | `INVITATION_SENT` | Yes | Emma sees that she invited David to Empty Planning Collection |
| `INV_PENDING_DAVID_C7` | David | `INVITATION_RECEIVED` | No | David sees a pending invitation from Emma |
| `INV_PENDING_FRANK_C7` | Emma | `INVITATION_SENT` | Yes | Emma sees that she invited Frank to Empty Planning Collection |
| `INV_PENDING_FRANK_C7` | Frank | `INVITATION_RECEIVED` | No | Frank sees a pending invitation from Emma |
| `INV_PENDING_ALICE_C8` | Frank | `INVITATION_SENT` | Yes | Frank sees that he invited Alice to Solo Coffee Run |
| `INV_PENDING_ALICE_C8` | Alice | `INVITATION_RECEIVED` | No | Alice sees a pending invitation from Frank |
| `INV_PENDING_BOB_C8` | Frank | `INVITATION_SENT` | Yes | Frank sees that he invited Bob as `EDITOR` |
| `INV_PENDING_BOB_C8` | Bob | `INVITATION_RECEIVED` | No | Bob sees a pending `EDITOR` invitation from Frank |
| `INV_PENDING_CAROL_C8` | Frank | `INVITATION_SENT` | Yes | Frank sees that he invited Carol to Solo Coffee Run |
| `INV_PENDING_CAROL_C8` | Carol | `INVITATION_RECEIVED` | No | Carol sees a pending invitation from Frank |
| `INV_PENDING_DAVID_C8` | Frank | `INVITATION_SENT` | Yes | Frank sees that he invited David to Solo Coffee Run |
| `INV_PENDING_DAVID_C8` | David | `INVITATION_RECEIVED` | No | David sees a pending invitation from Frank |
| `INV_PENDING_EMMA_C1` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited Emma to Bali Trip 2025 |
| `INV_PENDING_EMMA_C1` | Emma | `INVITATION_RECEIVED` | No | Emma sees a pending invitation from Alice |
| `INV_PENDING_FRANK_C2` | Bob | `INVITATION_SENT` | Yes | Bob sees that he invited Frank to Office Lunch Pool |
| `INV_PENDING_FRANK_C2` | Frank | `INVITATION_RECEIVED` | No | Frank sees a pending invitation from Bob |
| `INV_PENDING_EMMA_C3` | Bob | `INVITATION_SENT` | Yes | Bob sees that he invited Emma to SG Weekend Getaway |
| `INV_PENDING_EMMA_C3` | Emma | `INVITATION_RECEIVED` | No | Emma sees a pending invitation from Bob |
| `INV_PENDING_ALICE_C5` | David | `INVITATION_SENT` | Yes | David sees that he invited Alice to Holiday Dinner Party |
| `INV_PENDING_ALICE_C5` | Alice | `INVITATION_RECEIVED` | No | Alice sees a pending invitation from David |
| `INV_PENDING_EMMA_C8` | Frank | `INVITATION_SENT` | Yes | Frank sees that he invited Emma to Solo Coffee Run |
| `INV_PENDING_EMMA_C8` | Emma | `INVITATION_RECEIVED` | No | Emma sees a pending invitation from Frank |
| `INV_PENDING_FRANK_C9` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited Frank to the long-name workshop |
| `INV_PENDING_FRANK_C9` | Frank | `INVITATION_RECEIVED` | No | Frank sees a pending invitation from Alice |
| `INV_DECLINED_DAVID_C2` | Bob | `INVITATION_SENT` | Yes | Bob sees that he invited David to Office Lunch Pool |
| `INV_DECLINED_DAVID_C2` | David | `INVITATION_RECEIVED` | Yes | David sees the original invitation from Bob before rejecting |
| `INV_DECLINED_DAVID_C2` | David | `INVITATION_DECLINED_CONFIRMATION` | Yes | David sees that he rejected Bob's invitation |
| `INV_DECLINED_DAVID_C2` | Bob | `INVITATION_REJECTED` | No | Bob sees that David rejected the invitation |
| `INV_DECLINED_CAROL_C3` | Bob | `INVITATION_SENT` | Yes | Bob sees that he invited Carol to SG Weekend Getaway |
| `INV_DECLINED_CAROL_C3` | Carol | `INVITATION_RECEIVED` | Yes | Carol sees the original invitation from Bob before rejecting |
| `INV_DECLINED_CAROL_C3` | Carol | `INVITATION_DECLINED_CONFIRMATION` | Yes | Carol sees that she rejected Bob's invitation |
| `INV_DECLINED_CAROL_C3` | Bob | `INVITATION_REJECTED` | No | Bob sees that Carol rejected the invitation |
| `INV_DECLINED_BOB_C5` | David | `INVITATION_SENT` | Yes | David sees that he invited Bob to Holiday Dinner Party |
| `INV_DECLINED_BOB_C5` | Bob | `INVITATION_RECEIVED` | Yes | Bob sees the original invitation from David before rejecting |
| `INV_DECLINED_BOB_C5` | Bob | `INVITATION_DECLINED_CONFIRMATION` | Yes | Bob sees that he rejected David's invitation |
| `INV_DECLINED_BOB_C5` | David | `INVITATION_REJECTED` | No | David sees that Bob rejected the invitation |
| `INV_DECLINED_DAVID_C9` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited David as `EDITOR` |
| `INV_DECLINED_DAVID_C9` | David | `INVITATION_RECEIVED` | Yes | David sees the original invitation from Alice before rejecting |
| `INV_DECLINED_DAVID_C9` | David | `INVITATION_DECLINED_CONFIRMATION` | Yes | David sees that he rejected Alice's invitation |
| `INV_DECLINED_DAVID_C9` | Alice | `INVITATION_REJECTED` | No | Alice sees that David rejected the invitation |
| `INV_DECLINED_EMMA_C10` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited Emma to Budget Road Trip |
| `INV_DECLINED_EMMA_C10` | Emma | `INVITATION_RECEIVED` | Yes | Emma sees the original invitation from Alice before rejecting |
| `INV_DECLINED_EMMA_C10` | Emma | `INVITATION_DECLINED_CONFIRMATION` | Yes | Emma sees that she rejected Alice's invitation |
| `INV_DECLINED_EMMA_C10` | Alice | `INVITATION_REJECTED` | No | Alice sees that Emma rejected the invitation |
| `INV_DECLINED_FRANK_C11` | Alice | `INVITATION_SENT` | Yes | Alice sees that she invited Frank to Flat Share Planning |
| `INV_DECLINED_FRANK_C11` | Frank | `INVITATION_RECEIVED` | Yes | Frank sees the original invitation from Alice before rejecting |
| `INV_DECLINED_FRANK_C11` | Frank | `INVITATION_DECLINED_CONFIRMATION` | Yes | Frank sees that he rejected Alice's invitation |
| `INV_DECLINED_FRANK_C11` | Alice | `INVITATION_REJECTED` | No | Alice sees that Frank rejected the invitation |

Source: `BACKEND/notification-service/src/main/java/com/mypay/notification/seed/SeedDataInitializer.java`

## Notification Seed Coverage

| Coverage Area | Seeded Data |
| --- | --- |
| Sent invitation | `INVITATION_SENT` for Alice, Bob, David, Emma, and Frank |
| Invitation received history | `INVITATION_RECEIVED` for every active seeded invitee: Alice, Bob, Carol, David, Emma, and Frank |
| Pending invitation dialog coverage | Unread `INVITATION_RECEIVED` for Frank, Emma, David, Bob, Carol, and Alice across 17 pending invitations |
| Accepted invitation | `INVITATION_ACCEPTED_CONFIRMATION` for Alice and `INVITATION_ACCEPTED` for Bob |
| Declined invitation | `INVITATION_DECLINED_CONFIRMATION` and `INVITATION_REJECTED` across 7 rejected invitation references |
| Unread badge coverage | Received invitations and inviter outcome notifications are unread |
| Read notification coverage | Sent and invitee confirmation notifications are read |

## Invitation Notification Interaction

No extra seed rows are required for the notification-to-invitation dialog. The existing pending `INVITATION_RECEIVED` seed notifications already use the matching invitation ID as `notf_ref_id`, which lets the frontend open the invitation response dialog from `/app/notifications`.

| User | Notification | Referenced Invitation | Expected UI Behavior |
| --- | --- | --- | --- |
| Frank | `INVITATION_RECEIVED` | `INV_PENDING_FRANK`, `INV_PENDING_FRANK_C7`, `INV_PENDING_FRANK_C2`, `INV_PENDING_FRANK_C9` | Clicking the notification opens Accept, Reject, and Ignore actions |
| Emma | `INVITATION_RECEIVED` | `INV_PENDING_EMMA_C11`, `INV_PENDING_EMMA_C1`, `INV_PENDING_EMMA_C3`, `INV_PENDING_EMMA_C8` | Clicking the notification opens Accept, Reject, and Ignore actions |
| David | `INVITATION_RECEIVED` | `INV_PENDING_DAVID_C11`, `INV_PENDING_DAVID_C7`, `INV_PENDING_DAVID_C8` | Clicking the notification opens Accept, Reject, and Ignore actions |
| Bob | `INVITATION_RECEIVED` | `INV_PENDING_BOB_C7`, `INV_PENDING_BOB_C8` | Clicking the notification opens Accept, Reject, and Ignore actions |
| Carol | `INVITATION_RECEIVED` | `INV_PENDING_CAROL_C7`, `INV_PENDING_CAROL_C8` | Clicking the notification opens Accept, Reject, and Ignore actions |
| Alice | `INVITATION_RECEIVED` | `INV_PENDING_ALICE_C8`, `INV_PENDING_ALICE_C5` | Clicking the notification opens Accept, Reject, and Ignore actions |

Accepting or rejecting submits the invitation response and marks the notification as read. Ignoring only marks the notification as read and leaves the invitation pending in the Invitations page.

## Docker Seed Profile

`notification-service` uses `SPRING_PROFILES_ACTIVE=docker,dev` in `BACKEND/docker-compose.yml` so these notification seeds are inserted when the Docker stack starts.

## Verification Queries

Run these against the Docker MySQL container to confirm invitation notification coverage:

```powershell
docker exec mypay-mysql mysql -uroot -proot -D ewallet_notification_db -e "SELECT notf_type, COUNT(*) AS count FROM notification_t WHERE notf_type LIKE 'INVITATION%' GROUP BY notf_type ORDER BY notf_type;"
```

Expected seeded counts, before any runtime-created invitations:

| Notification Type | Count |
| --- | ---: |
| `INVITATION_ACCEPTED` | 1 |
| `INVITATION_ACCEPTED_CONFIRMATION` | 1 |
| `INVITATION_DECLINED_CONFIRMATION` | 7 |
| `INVITATION_RECEIVED` | 25 |
| `INVITATION_REJECTED` | 7 |
| `INVITATION_SENT` | 25 |

If the database already contains runtime-created invitations, counts may be higher. To inspect the seeded rows by invitation reference:

```powershell
docker exec mypay-mysql mysql -uroot -proot -D ewallet_notification_db -e "SELECT notf_user_id, notf_type, notf_title, notf_ref_id, notf_read FROM notification_t WHERE notf_ref_id LIKE '400000%' ORDER BY notf_ref_id, notf_type, notf_user_id;"
```

## Other Seed Data To Review Before Removing

These are similar edge-case seeds that may be useful during testing, but should be reviewed before pruning because each still covers a distinct behavior:

| Seed Data | Current Purpose | Recommendation |
| --- | --- | --- |
| Grace inactive account | Tests inactive/quarantined auth and notification preference behavior | Keep unless inactive-login/status testing is no longer needed |
| Empty Planning Collection | Tests empty collection and no-expense UI states | Keep, now owned by Emma |
| Solo Coffee Run | Tests single-member/no-debt split behavior | Keep, now owned by Frank |
| Archived Movie Night | Tests closed/read-only collection behavior | Keep unless closed collection behavior is no longer displayed |
| Long-name collection and expense | Tests frontend wrapping and report layout boundaries | Keep for phone and responsive UI testing |
| Zero-balance USD wallet for Emma | Tests wallet display of zero balance/currency edge | Review if wallet UI no longer needs zero-balance coverage |
| Failed/reversed transactions | Tests error, reversal, and notification states | Keep if transaction history needs non-happy-path examples |
