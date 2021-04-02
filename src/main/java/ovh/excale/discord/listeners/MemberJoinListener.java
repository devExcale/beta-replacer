package ovh.excale.discord.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.dv8tion.jda.api.requests.ErrorResponse.MISSING_PERMISSIONS;

public class MemberJoinListener extends ListenerAdapter {

	private static boolean isVowel(char c) {
		return c == 'A' | c == 'E' | c == 'I' | c == 'O' | c == 'U' | c == 'a' | c == 'e' | c == 'i' | c == 'o' | c == 'u';
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {

		Guild guild = event.getGuild();
		Member member = event.getMember();

		String name = member.getEffectiveName();
		char[] arr = name.toCharArray();

		if(isVowel(name.charAt(0)))
			arr[0] = String.valueOf(arr[0])
					.toLowerCase()
					.charAt(0);
		else
			arr = Arrays.copyOfRange(arr, 1, arr.length);

		String bnick = '\u03B2' + String.valueOf(arr);

		// TODO: FIX: Cannot perform action due to a lack of Permission. Missing permission: NICKNAME_MANAGE
		member.modifyNickname(bnick)
				.reason("Beta-replacing")
				.queue(unused -> {}, t -> {

					// On missing permissions
					if(t instanceof ErrorResponseException && MISSING_PERMISSIONS.equals(((ErrorResponseException) t).getErrorResponse()))
						// Get owner
						guild.retrieveOwner(false)
								// Open dm
								.flatMap(owner -> owner.getUser()
										.openPrivateChannel())
								// Send error message
								.flatMap(dm -> dm.sendMessage(
										"I'm missing NICKNAME_MANAGE permission on your guild " + guild.getName() + ", so I can't beta-replace " + name + "'s nick!"))
								// Execute
								.queue();

				});

	}

}
