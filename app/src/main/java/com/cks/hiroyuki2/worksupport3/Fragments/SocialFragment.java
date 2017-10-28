/*
 * Copyright (c) $year. Hiroyuki Tamura All rights reserved.
 */

package com.cks.hiroyuki2.worksupport3.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cks.hiroyuki2.worksupport3.Activities.AddGroupActivity;
import com.cks.hiroyuki2.worksupport3.Activities.MainActivity;
import com.cks.hiroyuki2.worksupport3.Adapters.SocialGroupListRVAdapter;
import com.cks.hiroyuki2.worksupport3.Adapters.SocialListRVAdapter;
import com.cks.hiroyuki2.worksupport3.Entity.Group;
import com.cks.hiroyuki2.worksupport3.Entity.GroupInUserDataNode;
import com.cks.hiroyuki2.worksupport3.Entity.User;
import com.cks.hiroyuki2.worksupport3.FbCheckAndWriter;
import com.cks.hiroyuki2.worksupport3.R;
import com.cks.hiroyuki2.worksupport3.RecordDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.view.View.VISIBLE;
import static com.cks.hiroyuki2.worksupport3.Entity.Group.makeGroupFromSnap;
import static com.cks.hiroyuki2.worksupport3.Entity.User.makeUserFromSnap;
import static com.cks.hiroyuki2.worksupport3.FbCheckAndWriter.CODE_UPDATE_CHILDREN;
import static com.cks.hiroyuki2.worksupport3.FirebaseConnection.getRef;
import static com.cks.hiroyuki2.worksupport3.FirebaseConnection.getRootRef;
import static com.cks.hiroyuki2.worksupport3.Util.DEFAULT;
import static com.cks.hiroyuki2.worksupport3.Util.PREF_KEY_GROUP_USER_DATA;
import static com.cks.hiroyuki2.worksupport3.Util.PREF_NAME;
import static com.cks.hiroyuki2.worksupport3.Util.logStackTrace;
import static com.cks.hiroyuki2.worksupport3.Util.makeScheme;
import static com.cks.hiroyuki2.worksupport3.Util.onError;
import static com.cks.hiroyuki2.worksupport3.Util.setImgFromStorage;
import static com.cks.hiroyuki2.worksupport3.Util.toastNullable;

@EFragment(R.layout.fragment_social2)
public class SocialFragment extends Fragment implements ValueEventListener {

    private static final String TAG = "MANUAL_TAG: " + SocialFragment.class.getSimpleName();
    private static final int REQ_CODE_CREATE_GROUP = 1632;

    @ViewById(R.id.name) TextView myNameTv;
    @ViewById(R.id.icon) CircleImageView myIconIv;
    @ViewById(R.id.recycler_user) RecyclerView rvUser;
    @ViewById(R.id.recycler_share) RecyclerView rvShare;
    @ViewById(R.id.scroll) ScrollView sv;
    SocialListRVAdapter userAdapter;
    SocialGroupListRVAdapter groupAdapter;
    private List<User> userList = new ArrayList<>();
    private List<GroupInUserDataNode> groupList;

    private IOnCompleteGroup mListener;
    private User me;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = new User(FirebaseAuth.getInstance().getCurrentUser());
//        userList = FriendJsonEditor.generateUserList(getContext());
//        groupList = generateGroupList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof IOnCompleteGroup) {
            mListener = (IOnCompleteGroup) context;
        }
    }

    @AfterViews
    void onAfterViews(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            return;//エラー処理？？
        }

        myNameTv.setText(user.getDisplayName());
        setImgFromStorage(user, myIconIv, R.drawable.ic_face_origin_48dp);

        rvUser.setNestedScrollingEnabled(false);
        rvUser.setLayoutManager(new LinearLayoutManager(getContext()));

        rvShare.setNestedScrollingEnabled(false);
        rvShare.setLayoutManager(new LinearLayoutManager(getContext()));

        retrieveUserList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        context = null;
    }

    @Click(R.id.add_user)
    void showAddFriendActivity(){
        Intent intent = new Intent(context, com.cks.hiroyuki2.worksupport3.Activities.AddFriendActivity_.class);//要パッケージ名指定。importするとコンパイル通らず @see https://goo.gl/ru1n1x
        startActivity(intent);
    }

//    void showGroupBtn(){
//        rl.setVisibility(View.VISIBLE);
//    }

    @Click(R.id.my_profile)
    void onClickMyProf(){
        ((MainActivity)context).editMyProf();
    }

    @Click(R.id.add_group)
    void onClickAddGroup(){
//        ((SocialListRVAdapter)rvUser.getAdapter()).setChooseItemMode();
        Intent intent = new Intent(getActivity(), com.cks.hiroyuki2.worksupport3.Activities.AddGroupActivity_.class);//要パッケージ名指定。importするとコンパイル通らず @see https://goo.gl/ru1n1x
        intent.putParcelableArrayListExtra("userList", (ArrayList<? extends Parcelable>) userList);
        ((MainActivity)context).startActivityFromFragment(this, intent, REQ_CODE_CREATE_GROUP);
    }

    public void showBoard(@NonNull GroupInUserDataNode groupNode){
        if (mListener != null)
            mListener.onClickGroupItem(groupNode);
    }

    public void updateFriend(@NonNull List<User> newUserList, List<String> newUids){
        if (userAdapter != null)
            userAdapter.updateAllItem(newUserList, newUids);
    }

    @OnActivityResult(REQ_CODE_CREATE_GROUP)
    void onResultCreateGroup(Intent data, int resultCode,
                             @OnActivityResult.Extra(AddGroupActivity.INTENT_BUNDLE_GROUP_NAME) final String groupName,
                             @OnActivityResult.Extra(AddGroupActivity.INTENT_BUNDLE_GROUP_PHOTO_URL) @Nullable String photoUrl){

        if (resultCode != RESULT_OK) return;
        final String key = getRootRef().child("group").push().getKey();

        List<User> userListOpe = new ArrayList<>(userList);
        me.setChecked(true);
        userListOpe.add(me);
        userListOpe.remove(0);/*DEFAULT値を取り除く DEFAULT値をどうするかは後で考えましょう*/

        HashMap<String, Object> childMap = makeMap(me.getUserUid(), key, groupName, userListOpe, photoUrl);

        for (User user: userListOpe) {
            GroupInUserDataNode smGroup = new GroupInUserDataNode(groupName, key, photoUrl, user.equals(me));
            childMap.put(makeScheme("userData", user.getUserUid(), "group", key), smGroup);
        }

        childMap.put(makeScheme("calendar", key, DEFAULT), DEFAULT);

        final Group group = new Group(userListOpe, groupName, key, null, me.getUserUid(), photoUrl);//groupインスタンスを作成。
        getRootRef().updateChildren(childMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null){
                    Log.w(TAG, "onComplete: " + databaseError.getMessage());
                } else {
                    if (mListener != null)
                        mListener.onCompleteGroup(group);
                }
            }
        });
    }

    @OnActivityResult(SocialGroupListRVAdapter.CALLBACK_GROUP_NON_ADDED)
    void onResultGroupNonAdd(Intent data, int resultCode,
                             @OnActivityResult.Extra(SocialGroupListRVAdapter.GROUP) final GroupInUserDataNode groupNode,
                             @OnActivityResult.Extra(RecordDialogFragment.DIALOG_BUTTON) int button){
        if (resultCode == RESULT_OK) return;

//        final GroupInUserDataNode groupNode = (GroupInUserDataNode)data.getSerializableExtra(SocialGroupListRVAdapter.GROUP);
//        int button = data.getIntExtra(RecordDialogFragment.DIALOG_BUTTON, Integer.MAX_VALUE);
        if (button == BUTTON_POSITIVE){
            if (mListener == null) return;

            groupNode.added = true;
            DatabaseReference checkRef = getRef("group", groupNode.groupKey);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(makeScheme("group", groupNode.groupKey, "member", me.getUserUid(), "added"), true);
            hashMap.put(makeScheme("userData", me.getUserUid(), "group", groupNode.groupKey, "added"), true);
            final FbCheckAndWriter writer = new FbCheckAndWriter(checkRef, getRootRef(), context, hashMap) {
                @Override
                public void onSuccess(DatabaseReference ref) {
                    toastNullable(getContext(), R.string.msg_add_group);

                    SharedPreferences pref = getContext().getSharedPreferences(PREF_NAME, MODE_PRIVATE);//todo getContext()==nullのとき落ちるぜ
                    SharedPreferences.Editor editor = pref.edit();
                    String s = new Gson().toJson(groupNode);
                    editor.putString(PREF_KEY_GROUP_USER_DATA, s);//todo これおかしい groupKeyで場合分けしなきゃ
                    editor.apply();
                }

                @Override
                protected void onNodeExist(@NonNull DataSnapshot dataSnapshot) {
                    Group group = makeGroupFromSnap(getContext(), dataSnapshot, groupNode.groupKey);//todo getContext()==nullのとき落ちるぜ
                    if (group == null)
                        return;//例外処理はmakeGroupFromSnap()内で行っています

                    boolean isReallyMember = false;
                    for (User user: group.userList)
                        if (user.getUserUid().equals(me.getUserUid())){
                            user.setChecked(true);
                            isReallyMember = true;
                            break;
                        }

                    if (!isReallyMember){
                        onError(SocialFragment.this, TAG+"!isReallyMember", R.string.error);
                        return;
                    }

                    mListener.onCompleteGroup(group);
                    groupAdapter.notifyAddedToGroup(groupNode.groupKey);
                    super.onNodeExist(dataSnapshot);
//                  srl.setRefreshing(false);
                    }
                };
                writer.update(CODE_UPDATE_CHILDREN);
        } else if (button == BUTTON_NEGATIVE){
            exitGroup(me.getUserUid(), groupNode.groupKey, R.string.reject_invitation_toast);
        }
    }

    static HashMap<String, Object> makeMap(@NonNull String myUid, @NonNull String key, @NonNull String groupName, @NonNull List<User> userList, @Nullable String photoUrl){
        String photoUrlC = photoUrl;
        if (photoUrl == null)
            photoUrlC = "null";

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(makeScheme("group", key, "groupName"), groupName);
        hashMap.put(makeScheme("group", key, "host"), myUid);
        hashMap.put(makeScheme("group", key, "photoUrl"), photoUrlC);
        for (User user: userList) {
            hashMap.put(makeScheme("group", key, "member", user.getUserUid()), user);
        }
        return hashMap;
    }

    public void exitGroup(@NonNull String meUid, @NonNull final String groupKey, @StringRes final int toast){
        DatabaseReference checkRef = getRef("group", groupKey);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(makeScheme("group", groupKey, "member", meUid), null);
        hashMap.put(makeScheme("userData", meUid, "group", groupKey), null);
        FbCheckAndWriter writer = new FbCheckAndWriter(checkRef, getRootRef(), context, hashMap) {
            @Override
            public void onSuccess(DatabaseReference ref) {
                toastNullable(getContext(), toast);
                groupAdapter.notifyExitGroup(groupKey);
            }
        };
        writer.update(CODE_UPDATE_CHILDREN);
    }

    //自分は「参加」、他の人は「未参加」とする
    private List<User> createMemberList(@NonNull User me){
        List<User> userListOpe = new ArrayList<>(userList);
        userListOpe.remove(0);/*DEFAULT値を取り除く DEFAULT値をどうするかは後で考えましょう*/
//        for (User user: userListOpe)
//            user.setChecked(false);
//        me.setChecked(true);
//        userListOpe.add(me);
        return userListOpe;
    }

    public interface IOnCompleteGroup {
        void onCompleteGroup(@NonNull Group group);
        void onClickGroupItem(@NonNull GroupInUserDataNode groupNode);
    }

    private void retrieveUserList(){
        getRef("friend", me.getUserUid()).addListenerForSingleValueEvent(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()){/*このノードはNonNUllが保障されている*/
            onError(context, TAG+"!dataSnapshot.exists()"+dataSnapshot.toString(), R.string.error);
            return;
        }

        for (DataSnapshot child: dataSnapshot.getChildren()) {
            if (child.getKey().equals(DEFAULT))
                continue;
            userList.add(makeUserFromSnap(child));
        }

        userAdapter = new SocialListRVAdapter(userList, this);
        rvUser.setAdapter(userAdapter);

        getRef("userData", me.getUserUid(), "group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()){/*このノードはNonNUllが保障されている*/
                    onError(SocialFragment.this, TAG+"!dataSnapshot.exists()"+dataSnapshot.toString(), R.string.error);
                    return;
                }

                groupList = new ArrayList<>();
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    if (data.getKey().equals(DEFAULT))
                        continue;
                    GroupInUserDataNode groupNode = data.getValue(GroupInUserDataNode.class);
                    groupNode.groupKey = data.getKey();
                    groupList.add(groupNode);
                }
                groupAdapter = new SocialGroupListRVAdapter(groupList, SocialFragment.this);
                rvShare.setAdapter(groupAdapter);

                sv.setVisibility(VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onError(SocialFragment.this, TAG+databaseError.getDetails(), R.string.error);
            }
        });
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        logStackTrace(databaseError.toException());
        toastNullable(getContext(), R.string.error);
    }
}