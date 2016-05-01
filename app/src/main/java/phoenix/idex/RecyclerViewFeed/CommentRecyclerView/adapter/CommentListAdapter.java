package phoenix.idex.RecyclerViewFeed.CommentRecyclerView.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.List;

import phoenix.idex.Activities.EditProfileActivity;
import phoenix.idex.R;
import phoenix.idex.RecyclerViewFeed.CommentRecyclerView.data.CommentItem;
import phoenix.idex.RecyclerViewFeed.MainRecyclerView.app.AppController;
import phoenix.idex.ServerConnections.ServerRequests;
import phoenix.idex.UserLocalStore;
import phoenix.idex.VolleyServerConnections.VolleyComments;

/**
 * Created by Ravinder on 2/25/16.
 */
public class CommentListAdapter extends RecyclerSwipeAdapter<CommentListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private List<CommentItem> commentItems;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();;
    private ServerRequests serverRequests;
    private UserLocalStore userLocalStore;
    private View postView;
    private VolleyComments volleyComments;

    public CommentListAdapter(Context context, List<CommentItem> commentItems) {
        inflater = LayoutInflater.from(context);
        this.mContext = context;
        this.commentItems = commentItems;
        userLocalStore = new UserLocalStore(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Custom root of recycle view
         this.postView = inflater.inflate(R.layout.item_comment, parent, false);
        volleyComments = new VolleyComments(mContext);
        // Hold a structure of a view. See class viewholder, which holds the structure
        return new ViewHolder(this.postView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        serverRequests = new ServerRequests(mContext);

        // Initialize fonts
        Typeface killFillFont = Typeface.createFromAsset(mContext.getAssets(), "Menufont.ttf");
        holder.name.setTypeface(killFillFont);

        final CommentItem currentPos = commentItems.get(position);
        holder.name.setText(currentPos.getName());
        holder.txtComment.setText(currentPos.getComment());

        if(currentPos.getRecommended() == 1) {
            holder.imgRecommend.setBackgroundResource(android.R.drawable.btn_star_big_on);
        }


        // Logged in user's post will get delete option instead of report option
        if (currentPos.getUsername().equals(userLocalStore.getLoggedInUser().getUsername())) {
            holder.tvManagePost.setText("Delete");
            holder.tvEditPost.setVisibility(View.VISIBLE);

        } else {
            holder.bottomWrapper1.setWeightSum(1);
            holder.tvManagePost.setText("Report");
        }


        // Converting timestamp into x ago format
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                Long.parseLong(currentPos.getTimeStamp()),
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        holder.timestamp.setText(timeAgo);


        // If the user uploaded a new photo through the editing page, load current JSON and not Cache
        // This is achieved through setImageLoaderNull()
        if (EditProfileActivity.isNewPhotoUploaded()) {
            EditProfileActivity.setIsNewPhotoUploaded(false);
            AppController.getInstance().setImageLoaderNull();
        }
        imageLoader = AppController.getInstance().getImageLoader();

        // user profile pic
        if (!currentPos.getProfilePic().equals("")) {
            holder.profilePic.setImageUrl(currentPos.getProfilePic(), imageLoader);
        } else { // default
            holder.profilePic.setImageUrl("http://oi67.tinypic.com/24npqbk.jpg", imageLoader);
        }

        // Set total recommend count in the text view
        holder.tvRecommendedCount.setText(currentPos.getRecommendTotalCount() + "");

        // Recommend button
        holder.tvRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // The user already chose recommended, cancel it out
                if (currentPos.getRecommended() == 1) {
                    currentPos.setRecommended(0);
                    currentPos.minusRecommendTotal();
                    holder.tvRecommendedCount.setText(currentPos.getRecommendTotalCount() + "");
                    holder.imgRecommend.setBackgroundResource(android.R.drawable.star_off);

                    // Remove from user's recommended list and minus one from total recommended count
                    volleyComments.removeFromRecommendedList(currentPos.getCommentID(), userLocalStore.getLoggedInUser().getUserID());
                    volleyComments.updateMinusARecommendCount(currentPos.getCommentID());
                } else {
                    // Add to user's recommended list and add one to the total recommended count
                    currentPos.setRecommended(1);
                    currentPos.addRecommendTotal();
                    holder.tvRecommendedCount.setText(currentPos.getRecommendTotalCount() + "");
                    holder.imgRecommend.setBackgroundResource(android.R.drawable.btn_star_big_on);

                    volleyComments.updateRecommend(currentPos.getCommentID());
                    volleyComments.addToUserCommentList(currentPos.getCommentID(), userLocalStore.getLoggedInUser().getUserID());
                }
            }
        });


        holder.tvManagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.tvManagePost.getText().toString().equals("Delete")) {

                    serverRequests.deleteAPostInBackground(currentPos.getPostId());
                } else {
                    Toast.makeText(v.getContext(), "Clicked on Report ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        // Drag From Left
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, holder.swipeLayout.findViewById(R.id.bottom_wrapper1));

        // Drag From Right
        holder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, holder.swipeLayout.findViewById(R.id.bottom_wrapper));

        // Handling different events when swiping
        holder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });

        // mItemManger is member in RecyclerSwipeAdapter Class
        mItemManger.bindView(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return commentItems.size();
    }

    // Holder knows and references where the fields are
    public class ViewHolder extends RecyclerView.ViewHolder {
        SwipeLayout swipeLayout;
        NetworkImageView profilePic;
        TextView txtComment, timestamp, name, tvManagePost, tvEditPost, tvRecommend, tvRecommendedCount;
        LinearLayout bottomWrapper1;
        LinearLayout commentLayout;
        ImageView imgRecommend;


        public ViewHolder(View itemView) {
            super(itemView);

            txtComment = (TextView) itemView.findViewById(R.id.txtComment);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipeCommentLayout);
            tvManagePost = (TextView) itemView.findViewById(R.id.tvManagePost);
            profilePic = (NetworkImageView) itemView.findViewById(R.id.profilePicComment);
            name = (TextView) itemView.findViewById(R.id.nameComment);
            timestamp = (TextView) itemView.findViewById(R.id.timestampComment);
            tvEditPost = (TextView) itemView.findViewById(R.id.tvEditPost);
            bottomWrapper1 = (LinearLayout) itemView.findViewById(R.id.bottom_wrapper1);
            commentLayout = (LinearLayout) itemView.findViewById(R.id.commentLayout);
            tvRecommend = (TextView) itemView.findViewById(R.id.tvRecommend);
            tvRecommendedCount =  (TextView) itemView.findViewById(R.id.tvRecommenedCount);
            imgRecommend = (ImageView) itemView.findViewById(R.id.imgRecommend);


            /*
            tvRecommend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    CommentItem currentItem = commentItems.get(position);
                    volleyComments.updateRecommend(currentItem.getCommentID());
                    System.out.println("ID of comment is: " + currentItem.getCommentID());
                    Toast.makeText(mContext, "Adding to recommend...", Toast.LENGTH_SHORT).show();
                }
            });*/

        }

    }
    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipeCommentLayout;
    }
}