package in.toud.toud.ui;

import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import in.toud.toud.AppController;
import in.toud.toud.R;
import in.toud.toud.fragment.JIDListFragment;
import in.toud.toud.model.JID;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import in.toud.toud.model.User;
import in.toud.toud.service.XMMPService;
import in.toud.toud.service.listener.ChatHolderListener;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;


public class MainActivity extends ActionBarActivity implements JIDListFragment.OnFragmentInteractionListener {

    public static final String EXTRA_NAME = "cheese_name";

    @Bind(R.id.appbar)
    AppBarLayout mAppBarLayout;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @Bind(R.id.viewpager)
    ViewPager mViewPager;

    /**
     * Yours JID
     */
    JID you;
    private Realm realm;
    private final String DEBUG_TAG = "TOOOOOOOOOOOOoUD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


/*        RealmConfiguration realmConfig = new RealmConfiguration.Builder(this).build();
        Realm.deleteRealm(realmConfig);
        realm = Realm.getInstance(AppController.getAppContext());
        realm.beginTransaction();
        realm.commitTransaction();*/

        // Add a person
        realm = Realm.getInstance(AppController.getAppContext());

        User yourJID = new User();
        yourJID.setUsername("roshan@192.168.1.6");
        yourJID.setPassword("roshan");
        yourJID.setNickName("Roshan Piyush");
        yourJID.setIsAvailable(true);

        final ActionBar ab = getSupportActionBar();
        ab.setTitle(yourJID.getUsername());
        ab.setDisplayHomeAsUpEnabled(true);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(yourJID);
        realm.commitTransaction();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            setupViewPager(viewPager);
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        XMMPService service = new XMMPService();
        Realm realm = Realm.getInstance(AppController.getAppContext());
        RealmQuery query = realm.where(User.class);
        RealmObject userRealmObject = query.findFirst();
        User myself = (User) userRealmObject;
        service.connect(myself);
    }

    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
        //service.sendMessageToChat();
        //service.addRoster();
        Log.d(DEBUG_TAG, "I am clicking now");
    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(JIDListFragment.newInstance(), "Category 1");
        adapter.addFragment(JIDListFragment.newInstance(), "Category 1");
        viewPager.setAdapter(adapter);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

}