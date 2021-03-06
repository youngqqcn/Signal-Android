package org.thoughtcrime.securesms.groups.ui.addtogroup;

import android.content.Context;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.groups.GroupChangeBusyException;
import org.thoughtcrime.securesms.groups.GroupChangeFailedException;
import org.thoughtcrime.securesms.groups.GroupId;
import org.thoughtcrime.securesms.groups.GroupInsufficientRightsException;
import org.thoughtcrime.securesms.groups.GroupManager;
import org.thoughtcrime.securesms.groups.GroupNotAMemberException;
import org.thoughtcrime.securesms.groups.MembershipNotSuitableForV2Exception;
import org.thoughtcrime.securesms.groups.ui.GroupChangeErrorCallback;
import org.thoughtcrime.securesms.groups.ui.GroupChangeFailureReason;
import org.thoughtcrime.securesms.logging.Log;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.recipients.RecipientId;
import org.thoughtcrime.securesms.util.concurrent.SignalExecutors;

import java.io.IOException;
import java.util.Collections;

final class AddToGroupRepository {

  private static final String TAG = Log.tag(AddToGroupRepository.class);

  private final Context context;

  AddToGroupRepository() {
    this.context = ApplicationDependencies.getApplication();
  }

  public void add(@NonNull RecipientId recipientId,
                  @NonNull Recipient groupRecipient,
                  @NonNull GroupChangeErrorCallback error,
                  @NonNull Runnable success)
  {
    SignalExecutors.UNBOUNDED.execute(() -> {
      try {
        GroupId.Push pushGroupId = groupRecipient.requireGroupId().requirePush();

        GroupManager.addMembers(context, pushGroupId, Collections.singletonList(recipientId));

        success.run();
      } catch (GroupInsufficientRightsException | GroupNotAMemberException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.NO_RIGHTS);
      } catch (GroupChangeFailedException | GroupChangeBusyException | IOException e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.OTHER);
      } catch (MembershipNotSuitableForV2Exception e) {
        Log.w(TAG, e);
        error.onError(GroupChangeFailureReason.NOT_CAPABLE);
      }
    });
  }
}
