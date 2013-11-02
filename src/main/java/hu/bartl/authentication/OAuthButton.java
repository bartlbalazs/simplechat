package hu.bartl.authentication;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;

/**
 * Starting point to create a {@link Button} that allows the user to log in
 * using OAuth; e.g log in with Facebook or Twitter.
 * <p>
 * Uses the Scribe oauth library, and it should be fairly straightforward to
 * implement a button for all supported services.
 * </p>
 * <p>
 * Generally, you just give the buttons the API keys that can be obtained from
 * the service in question, and a callback that will receive some user data once
 * the user has been authenticated. Some buttons implementations might provide
 * additional options (e.g get user email address from Facebook).
 * </p>
 * <p>
 * This approach is intentionally simplistic for this specific use-case: log in
 * with X. For more flexible OAuth interactions, the Scribe library can be used
 * directly.
 * </p>
 * <p>
 * March 2, 2013: Modified by asarraf21 to make it compatible by Vaadin 7.
 * </p>
 */
public abstract class OAuthButton extends Button {
	private static final long serialVersionUID = 6130786349178836887L;
	protected OAuthService service = null;
	protected Token requestToken = null;
	protected Token accessToken = null;

	protected String apiKey;
	protected String apiSecret;

	protected RequestHandler handler;

	protected OAuthListener authListener;

	/**
	 * @param caption
	 *            button caption
	 * @param apiKey
	 *            API key from the service providing OAuth
	 * @param apiSecret
	 *            API secret from the service providing OAuth
	 * @param authListener
	 *            called once the user has been authenticated
	 */
	public OAuthButton(String caption, String apiKey, String apiSecret,
			OAuthListener authListener) {
		super(caption);
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
		this.authListener = authListener;
		super.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 7481852288820112363L;

			@Override
			public void buttonClick(ClickEvent event) {
				authenticate();
			}
		});
	}

	/**
	 * Gets the URL that the user will be sent to in order to authenticate. Most
	 * implementations will also create the requestToken at this point.
	 * 
	 * @return authentication url for the OAuth service
	 */
	protected abstract String getAuthUrl();

	/**
	 * Gets the {@link Api} implementation class that this service uses.
	 * 
	 * @return {@link Api} implementation class
	 */
	protected abstract Class<? extends Api> getApi();

	/**
	 * Gets the name of the parameter that will contain the verifier when the
	 * user returns from the OAuth service.
	 * 
	 * @return verifier parameter name
	 */
	protected abstract String getVerifierName();

	private static final String[] oauthFails = new String[] { "oauth_problem" };

	/**
	 * Gets the names of parameters that the OAuth service uses to indicate a
	 * problem during authentication - e.g if the user presses 'Cancel' at the
	 * authentication page.
	 * 
	 * @return
	 */
	protected String[] getFailureParameters() {
		return oauthFails;
	}

	/**
	 * Gets the URL from which JSON formatted user data can be fetched.
	 * 
	 * @return JSON user data url
	 */
	protected abstract String getJsonDataUrl();

	/**
	 * Gets the {@link OAuthUser} implementation class for the user data that
	 * this service provides.
	 * 
	 * @return {@link OAuthUser} implementation class
	 */
	protected abstract Class<? extends OAuthUser> getUserClass();

	/**
	 * Gets the OAuth service singleton.
	 * 
	 * @return OAuth service singleton
	 */
	protected OAuthService getService() {
		if (service == null) {
			service = new ServiceBuilder().provider(getApi()).apiKey(apiKey)
					.apiSecret(apiSecret)
					.callback(Page.getCurrent().getLocation().toString())
					.build();
		}
		return service;
	}

	/**
	 * Connects the parameter handler that will be invoked when the user comes
	 * back, and sends the user to the authentication url for the OAuth service.
	 */
	protected void authenticate() {
		if (handler == null) {
			handler = createRequestHandler();
			getSession().addRequestHandler(handler);
		}
		Page.getCurrent().open(getAuthUrl(), "_self");
	}

	/**
	 * Creates the parameter handler that will be invoked when the user returns
	 * from the OAuth service.
	 * 
	 * @return the parameter handler
	 */
	protected RequestHandler createRequestHandler() {
		return new RequestHandler() {
			private static final long serialVersionUID = 3102955648855910068L;

			@Override
			public boolean handleRequest(VaadinSession session,
					VaadinRequest request, VaadinResponse response)
					throws IOException {

				Map<String, String[]> parameters = request.getParameterMap();

				try {
					String v = parameters.get(getVerifierName())[0];
					Verifier verifier = new Verifier(v);

					accessToken = service
							.getAccessToken(requestToken, verifier);

					OAuthUser user = getUser();

					if (getSession() != null) {
						getSession().removeRequestHandler(handler);
					}
					handler = null;
					authListener.userAuthenticated(user);

				} catch (NullPointerException | OAuthException e) {
					if (getFailureParameters() != null) {
						for (String key : getFailureParameters()) {
							if (parameters.containsKey(key)) {
								authListener.failed(parameters.get(key)[0]);
								break;
							}
						}
					}
				}
				return false;
			}
		};

	}

	/**
	 * Creates and returns the {@link OAuthUser} instance, usually by retreiving
	 * JSON data from the url provided by {@link #getJsonDataUrl()}.
	 * 
	 * @return the {@link OAuthUser} instance containing user data from the
	 *         service
	 */
	protected OAuthUser getUser() {
		OAuthRequest request = new OAuthRequest(Verb.GET, getJsonDataUrl());
		service.signRequest(accessToken, request);
		Response response = request.send();

		Gson gson = new Gson();
		OAuthUser user = gson.fromJson(response.getBody(), getUserClass());

		// TODO set the token/secret here?
		try {
			Field tokenField = user.getClass().getDeclaredField("token");
			if (tokenField != null) {
				tokenField.setAccessible(true);
				tokenField.set(user, accessToken.getToken());
			}

			Field tokenSecretField = user.getClass().getDeclaredField(
					"tokenSecret");
			if (tokenSecretField != null) {
				tokenSecretField.setAccessible(true);
				tokenSecretField.set(user, accessToken.getSecret());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return user;
	}

	/**
	 * Called when the {@link OAuthUser} instance has been successfully created,
	 * or the OAuth service returned a problem code.
	 */
	public interface OAuthListener {
		public void userAuthenticated(OAuthUser user);

		public void failed(String reason);
	}

	/**
	 * Contains user data common for most services. Some services might add own
	 * data, or leave some data as null - for instance 'email' is quite seldom
	 * available trough the APIs.
	 * <p>
	 * The default {@link OAuthButton#getUser()} implementation sets the 'token'
	 * and 'tokenSecret' member fields if such exist, so that the
	 * {@link OAuthUser} implementation can just return these in
	 * {@link #getToken()} and {@link #getTokenSecret()}.
	 * </p>
	 */
	public static interface OAuthUser {

		/**
		 * Name of the OAuth service, e.g "facebook".
		 * 
		 * @return
		 */
		public String getService();

		/**
		 * Often "Firstname Lastname", but not always - e.g Twitter users have a
		 * single 'name' that can be changed to pretty much anything.
		 * 
		 * @return user name
		 */
		public String getName();

		/**
		 * The screen name is usually a short username used no the service, most
		 * often unique, and quite often used to identify the user profile (e.g
		 * http://twitter.com/screenname).
		 * 
		 * @return
		 */
		public String getScreenName();

		/**
		 * Url to the avatar picture for the user.
		 * 
		 * @return
		 */
		public String getPictureUrl();

		/**
		 * Id form the OAuth service; this is unique within the service. A
		 * "globaly unique" id can be created for instance by combining this id
		 * with the service name (e.g "facebook:12345").
		 * 
		 * @return
		 */
		public String getId();

		/**
		 * Url to the users public profile on the service (e.g
		 * http://twitter.com/screenname).
		 * 
		 * @return
		 */
		public String getPublicProfileUrl();

		/**
		 * Email address - NOTE that this is quite seldom provided. Also, it
		 * might be better to allow the user to provide an email address of
		 * choice while registering for your service.
		 * 
		 * @return email address or (quite often) null
		 */
		public String getEmail();

		/**
		 * Gets the OAuth access token that can be used together with the token
		 * secret ({@link #getTokenSecret()}) in order to access the OAuth
		 * service API.
		 * 
		 * @return OAuth access token
		 */
		public String getToken();

		/**
		 * Gets the OAuth access token secret that can be used together with the
		 * token ({@link #getToken()}) in order to access the OAuth service API.
		 * 
		 * @return OAuth access token secret
		 */
		public String getTokenSecret();

	}
}