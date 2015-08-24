/*
 * $Id$
 *
 * The MIT License (MIT)
 * Copyright (c) 2015 Threema GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE
 */
package ch.threema.apitool;

import ch.threema.apitool.console.commands.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Command line interface for {@link CryptTool} and {@link APIConnector} operations
 * for testing purposes and simple invocation from other programming languages.
 */
public class ConsoleMain {

	static class Commands {
		protected final List<CommandGroup> commandGroups = new ArrayList<>();

		public CommandGroup create(String description) {
			CommandGroup g = new CommandGroup(description);
			this.commandGroups.add(g);
			return g;
		}

		public ArgumentCommand find(String... arguments) {
			if(arguments.length > 0) {
				for (CommandGroup g : this.commandGroups) {
					ArgumentCommand c = g.find(arguments);
					if (c != null) {
						return c;
					}
				}
			}
			return null;
		}
	}

	static class CommandGroup {
		protected final String description;
		protected List<ArgumentCommand> argumentCommands = new ArrayList<>();

		CommandGroup(String description) {
			this.description = description;
		}

		public CommandGroup add(Command command, String... arguments) {
			this.argumentCommands.add(new ArgumentCommand(arguments, command));
			return this;
		}

		public ArgumentCommand find(String... arguments) {
			ArgumentCommand matchedArgumentCommand = null;
			int argMatchedSize = -1;
			for(ArgumentCommand c: this.argumentCommands) {
				boolean matched = true;
				int matchedSize = 0;
				for(int n = 0; n < c.arguments.length; n++) {
					if(n > arguments.length || !c.arguments[n].equals(arguments[n])) {
						matched = false;
						break;
					}
					else {
						matchedSize++;
					}
				}

				if(matched && matchedSize > argMatchedSize) {
					matchedArgumentCommand = c;
					argMatchedSize = matchedSize;
				}

			}
			return matchedArgumentCommand;
		}
	}

	static class ArgumentCommand {
		protected final String[] arguments;
		protected final Command command;

		ArgumentCommand(String[] arguments, Command command) {
			this.arguments = arguments;
			this.command = command;
		}

		public void run(String[] givenArguments) throws Exception {
			if(givenArguments.length < this.arguments.length) {
				throw new Exception("invalid arguments");
			}

			this.command.run((String[])ArrayUtils.subarray(givenArguments, this.arguments.length, givenArguments.length));
		}
	}

	private static final Commands commands = new Commands();

	public static void main(String[] args) throws Exception {

		commands.create("Local operations (no network communication)")
				.add(new EncryptCommand(), "-e")
				.add(new DecryptCommand(), "-d")
				.add(new HashEmailCommand(), "-h", "-e")
				.add(new HashPhoneCommand(), "-h", "-p")
				.add(new GenerateKeyPairCommand(), "-g")
				.add(new DerivePublicKeyCommand(), "-p");

		commands.create("Network operations")
				.add(new SendSimpleMessageCommand(), "-s")
				.add(new SendE2ETextMessageCommand(), "-S")
				.add(new SendE2EImageMessageCommand(), "-S", "-i")
				.add(new SendE2EFileMessageCommand(), "-S", "-f")
				.add(new IDLookupByEmail(), "-l", "-e")
				.add(new IDLookupByPhoneNo(), "-l", "-p")
				.add(new FetchPublicKey(), "-l", "-k")
				.add(new CapabilityCommand(), "-c")
				.add(new DecryptAndDownloadCommand(), "-D");


		ArgumentCommand argumentCommand = commands.find(args);
		if(argumentCommand == null) {
			usage(args.length == 1 && args[0].equals("html"));
		}
		else {
			argumentCommand.run(args);
		}
	}

	private static void usage(boolean htmlOutput) {
		if(!htmlOutput) {
			System.out.println("version:" + ConsoleMain.class.getPackage().getImplementationVersion());

			System.out.println("usage:\n");

			System.out.println("General information");
			System.out.println("-------------------\n");

			System.out.println("Where a key needs to be specified, it can either be given directly as");
			System.out.println("a command line parameter (in hex with a prefix indicating the type;");
			System.out.println("not recommended on shared machines as other users may be able to see");
			System.out.println("the arguments), or as the path to a file that it should be read from");
			System.out.println("(file contents also in hex with the prefix).\n");
		}

		String groupDescriptionTemplate = htmlOutput ? "<h3>%s</h3>\n" : "\n%s\n" + StringUtils.repeat("-", 80) + "\n\n";
		String commandTemplate = htmlOutput ? "<pre><code>java -jar threema-msgapi-tool.jar %s</code></pre>\n" : "%s\n";

		for(CommandGroup commandGroup: commands.commandGroups) {
			System.out.format(groupDescriptionTemplate, commandGroup.description);

			for(ArgumentCommand argumentCommand: commandGroup.argumentCommands) {
				StringBuilder command = new StringBuilder();
				for(int n = 0; n < argumentCommand.arguments.length; n++) {
					command.append(argumentCommand.arguments[n])
							.append(" ");
				}
				String argumentDescription = argumentCommand.command.getUsageArguments();
				if(htmlOutput) {
					System.out.format("<h4>%s</h4>\n", argumentCommand.command.getSubject());
					argumentDescription = StringEscapeUtils.escapeHtml(argumentDescription);
				}
				command.append(argumentDescription);

				System.out.format(commandTemplate, command.toString().trim());

				String description = argumentCommand.command.getUsageDescription();
				if(htmlOutput) {
					System.out.format("<p>%s</p>\n\n", description);
				}
				else {
					System.out.println("   " + WordUtils.wrap(description, 76, "\n   ", false));
					System.out.println("");
				}
			}
		}
	}
}
