package com.RedditDataScraper;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;

public class RedditResult {
	private final MainJFormFrame mainJFormFrame;
	private final MyRedditClient myRedditClient;
	private final AllFilters filterArrayList;
	private String subredditTextField;
	private String keywordTextField;
	private String nsfw;
	private String spoiler;
	private DateTimeFormatter dateFormatter;
	private LocalDateTime now;
	private int index;

	public RedditResult(MainJFormFrame mainJFormFrame, MyRedditClient myRedditClient) {
		this.mainJFormFrame = mainJFormFrame;
		this.myRedditClient = myRedditClient;
		filterArrayList = new AllFilters();
		dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		now = LocalDateTime.now();
		index = 0;

		nsfw = "No";
		spoiler = "No";
	}

	public void addFilters() {
		subredditTextField = mainJFormFrame.getSubredditTextField().getText().replaceAll(" ", "");
		keywordTextField = mainJFormFrame.getKeywordTextField().getText();
		if (mainJFormFrame.getNoNsfwCheckBox().isSelected()) {
			filterArrayList.addFilter(new NoNsfwFilter());
		}
		if (mainJFormFrame.getNoSpoilerCheckBox().isSelected()) {
			filterArrayList.addFilter(new NoSpoilerFilter());
		}
		if (mainJFormFrame.getScoreFilterCheckBox().isSelected()) {
			System.out.println(mainJFormFrame.getScoreMinTextField().getText());
			int scoreMin = Integer.parseInt(mainJFormFrame.getScoreMinTextField().getText().replaceAll(",", ""));
			int scoreMax = Integer.parseInt(mainJFormFrame.getScoreMaxTextField().getText().replaceAll(",", ""));
			if (scoreMax == 0) {
				filterArrayList.addFilter(new ScoreFilter(scoreMin));
			} else {
				filterArrayList.addFilter(new ScoreFilter(scoreMin, scoreMax));
			}
		}
		if (mainJFormFrame.getCommentCountFilterCheckBox().isSelected()) {
			int commentMin = Integer.parseInt(mainJFormFrame.getCommentCountMinTextField().getText().replaceAll(",", ""));
			int commentMax = Integer.parseInt(mainJFormFrame.getCommentCountMaxTextField().getText().replaceAll(",", ""));
			if (commentMax == 0) {
				filterArrayList.addFilter(new CommentCountFilter(commentMin));
			} else {
				filterArrayList.addFilter(new CommentCountFilter(commentMin, commentMax));
			}
		}

		if (subredditTextField.equals("")) {
			//filterArrayList.addFilter(new SubredditFilter(subredditTextField.split(",")));
			subredditTextField = "all";
		}
		if (mainJFormFrame.getRegexCheckBox().isSelected()) {
			filterArrayList.addFilter(new KeywordFilter(keywordTextField));
		} else {
			filterArrayList.addFilter(new KeywordFilter(keywordTextField.replaceAll(" ", "").split(",")));
			System.out.println(keywordTextField.replaceAll(" ", "").split(","));
		}
	}

	public Comparator<Submission> getSortSettings() {
		Comparator<Submission> comparator;
		if (mainJFormFrame.getAzSort().isSelected()) {
			comparator = new CompareTitle();
		} else if (mainJFormFrame.getZaSort().isSelected()) {
			comparator = new CompareTitle().reversed();
		} else if (mainJFormFrame.getScoreSortMin().isSelected()) {
			comparator = new CompareScore();
		} else {
			mainJFormFrame.getScoreSortMax().setSelected(true);
			comparator = new CompareScore().reversed();
		}
		return comparator;
	}

	public void display() {
		DefaultPaginator<Submission> paginator = myRedditClient
				.getMyclient()
				.subreddit(subredditTextField)
				//.subreddits("world","politics", subreddits[0])
				.posts()
				.sorting((SubredditSort) mainJFormFrame.getRedditSortComboBox().getSelectedItem())
				.timePeriod((TimePeriod) mainJFormFrame.getTimeComboBox().getSelectedItem())
				.build();

		Listing<Submission> nextPage = paginator.next();
		Comparator<Submission> comparator = getSortSettings();
		Collections.sort(nextPage, comparator);

		for (Submission s : nextPage) {
			if (filterArrayList.satisfies(s)) {
				if (s.isNsfw()) {
					nsfw = "Yes";
				}
				if (s.isSpoiler()) {
					spoiler = "Yes";
				}
				mainJFormFrame.getResultTextArea().append("Index: "
						+ index
						+ "\nSearch at: "
						+ dateFormatter.format(now)
						+ "\nTitle: "
						+ s.getTitle().replace('’', '\'').replace('—', '-')
						+ "\nScore: "
						+ s.getScore()
						+ "\nComment Count: "
						+ s.getCommentCount()
						+ "\nSubreddit: "
						+ s.getSubreddit()
						+ "\nURL: "
						+ s.getUrl()
						+ "\nPermalink: "
						+ "https://www.reddit.com" + s.getPermalink()
						+ "\nNSFW: "
						+ nsfw
						+ "\nSpoiler: "
						+ spoiler
						+ "\n"
						+ "\n");
				index++;
			}
		}
		filterArrayList.clear();
	}

}