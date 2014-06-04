package processmanager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;



/**
 * GitHubDataPlugin is a DataPlugin for GitHub. Users are GitHub users, and
 * Posts are repositories.
 */
public class GitHubProcess extends MigratableProcess {
    private final String NAME = "GitHub DataPlugin";
    final UserService service;
    public final static String gittoken = "45d262c43734f69d5c6f8005f3d0500b130bd2e4"; 

    /**
     * Authorizes the githubclient and services with the given token.
     */
    public GitHubProcess(String token) {
        super();
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(token);
        service = new UserService(client);
    }
    
    /**
     * Authorizes the github client/services with token in github.properties.
     */
    public GitHubProcess() {
        super();
        GitHubClient client = new GitHubClient();
        try {
            BufferedReader br = new BufferedReader(new FileReader("github.properties"));
            String token = br.readLine();
            br.close();
            client.setOAuth2Token(token);
        } catch (IOException e) {
            System.err.println("No token found, defaulting to no auth.");
        }
        service = new UserService(client);
    }
/*
    @Override
    public User getUserByUserName(String name) throws FrameworkException {
        try {
            org.eclipse.egit.github.core.User user = service.getUser(name);
            return new GithubUser(user);
        } catch (IOException e) {
            throw new FrameworkException("Couldn't get user " + name, e);
        }
    }

    @Override
    public List<String> getFollowersNames(User user) throws FrameworkException {
        List<String> friends = new ArrayList<String>();
        try {
            List<org.eclipse.egit.github.core.User> userList = service
                    .getFollowers(user.getUserName());
            for (org.eclipse.egit.github.core.User guser : userList)
                friends.add(guser.getLogin());
        } catch (IOException e) {
            throw new FrameworkException("Couldn't get followers.", e);
        }
        return friends;
    }

    @Override
    public List<String> getFollowingNames(User user) throws FrameworkException {
        List<String> friends = new ArrayList<String>();
        try {
            List<org.eclipse.egit.github.core.User> userList = service
                    .getFollowing(user.getUserName());
            for (org.eclipse.egit.github.core.User guser : userList)
                friends.add(guser.getLogin());
        } catch (IOException e) {
            throw new FrameworkException("Couldn't get following.", e);
        }
        return friends;
    }

    @Override
    public List<Post> getPosts(User user, Integer size) {
        List<Post> posts = new ArrayList<Post>();
        RepositoryService service = new RepositoryService();
        PageIterator<Repository> repoes = service.pageRepositories(
                user.getUserName(), size);
        for (Collection<Repository> repo : repoes)
            for (Repository rep : repo)
                posts.add(new GitHubPost(rep));
        return posts;
    }
    
    private class GithubUser implements User {
        private org.eclipse.egit.github.core.User user;
        private List<Post> posts;
        List<String> followers;
        List<String> following;

        public GithubUser(org.eclipse.egit.github.core.User user) throws FrameworkException {
            this.user = user;
            this.posts = GitHubDataPlugin.this.getPosts(this, 20);
            this.followers = GitHubDataPlugin.this.getFollowersNames(this);
            this.following = GitHubDataPlugin.this.getFollowingNames(this);
        }

        @Override
        public String getLocation() {
            return user.getLocation();
        }

        @Override
        public String getRealName() {
            return user.getName();
        }

        @Override
        public String getUserName() {
            return user.getLogin();
        }

        @Override
        public int getFollowing() {
            return user.getFollowing();
        }

        @Override
        public Integer getNumPosts() {
            return user.getPublicRepos();
        }

        @Override
        public String getUrl() {
            return user.getUrl();

        }

        @Override
        public Long getId() {
            return (long) user.getId();
        }

        @Override
        public List<Post> getPosts() {
            return posts;
        }

        @Override
        public String getDescription() {
            return user.getBlog();
        }

        @Override
        public int getFollowers() {
            return user.getFollowers();
        }

        @Override
        public String getEmail() {
            return user.getEmail();
        }

        @Override
        public List<String> getFollowingNames() {
            return following;
        }

        @Override
        public List<String> getFollowerNames() {
            return followers;
        }

    }

    private static class GitHubPost implements Post {
        private Repository repo;

        public GitHubPost(Repository repo) {
            this.repo = repo;
        }

        @Override
        public String getMessage() {
            return repo.getDescription();
        }

        @Override
        public String getAuthor() {
            return repo.getOwner().getName();
        }

        @Override
        public Date getTime() {
            return repo.getUpdatedAt();
        }

        @Override
        public int getLikes() {
            return repo.getWatchers();
        }
    }

    @Override
    public boolean canMakeRequest() {
        GitHubClient client = new GitHubClient();
        if (client.getRemainingRequests() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }
*/
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString(String[] paramArray) {
		// TODO Auto-generated method stub
		return null;
	}
}
