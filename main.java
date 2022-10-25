import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.util.Map.entry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class main
{
	public static HashMap<String, Integer> Symbols = new HashMap<>(Map.ofEntries(entry("R0", 0), entry("R1", 1),
			entry("R2", 2), entry("R3", 3), entry("R4", 4), entry("R5", 5), entry("R6", 6), entry("R7", 7),
			entry("R8", 8), entry("R9", 9), entry("R1O", 10), entry("R11", 11), entry("R12", 12), entry("R13", 13),
			entry("R14", 14), entry("R15", 15), entry("SCREEN", 16384), entry("KBD", 24576), entry("SP", 0),
			entry("LCL", 1), entry("ARG", 2), entry("THIS", 3), entry("THAT", 4)));

	public static int nextaddress = 16;

	public static void main(String[] args)
	{
		String filename = "";
		try
		{
			filename = args[0];
		} catch (Exception e)
		{
			System.out.println("Please specify a filename");
			System.exit(0);
		}
		ArrayList<String> instructions = get_instructions(filename);

		int counter = 0;
		for (int i = 0; i < instructions.size(); i++)
		{
			if (instructions.get(i).charAt(0) == '('
					&& instructions.get(i).charAt(instructions.get(i).length() - 1) == ')')
			{
				String loop = instructions.get(i).replace("(", "").replace(")", "");
				Symbols.put(loop, i - counter);
				counter++;
			}
		}

		for (int i = 0; i < instructions.size(); i++)
		{
			String binary_instruction = "";
			if (instructions.get(i).charAt(0) == '(')
			{
				instructions.remove(i);
				i--;
				continue;
			} else if (instructions.get(i).charAt(0) == '@')
			{
				binary_instruction = to_a(instructions.get(i));
			} else
			{
				binary_instruction = to_c(instructions.get(i));
			}
			System.out.println(binary_instruction);
			instructions.set(i, binary_instruction);
		}
		String outputname = filename.split(".asm")[0] + ".hack";
		Path output = Paths.get(outputname);
		try
		{
			Files.write(output, instructions, StandardCharsets.UTF_8);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static ArrayList<String> get_instructions(String filename)
	{
		Scanner kbd = null;
		try
		{
			kbd = new Scanner(new File(filename));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		ArrayList<String> instructions = new ArrayList<String>();
		while (kbd.hasNextLine())
		{
			String code = get_raw_code(kbd.nextLine());
			if (code != "")
				instructions.add(code);
		}
		return instructions;
	}

	public static String get_raw_code(String line)
	{
		return line.split("/")[0].replace(" ", "").replace("\r\n", "");
	}

	public static String to_a(String line)
	{
		String address = line.substring(1);
		try
		{
			return get_bin(Integer.parseInt(address));
		} catch (Exception e)
		{
			return get_bin(parse_symbol(address));
		}
	}

	public static int parse_symbol(String symbol)
	{
		if (Symbols.containsKey(symbol) == false)
		{
			Symbols.put(symbol, nextaddress);
			nextaddress++;
		}
		return Symbols.get(symbol);
	}

	public static String to_c(String line)
	{
		String output = "111";
		String dest = "";
		String jmp = "";
		String comp = "";

		if (line.contains("="))
		{
			dest = line.split("=")[0];
			line = line.split("=")[1];
		}
		if (line.contains(";"))
		{
			comp = line.split(";")[0];
			jmp = line.split(";")[1];
		} else
		{
			comp = line;
		}
		return output + get_bin_comp(comp) + get_bin_dest(dest) + get_bin_jmp(jmp);
	}

	public static String get_bin_dest(String dest)
	{
		if (dest != "")
		{
			String ans = "";
			if (dest.contains("A"))
			{
				ans += 1;
			} else
			{
				ans += 0;
			}
			if (dest.contains("D"))
			{
				ans += 1;
			} else
			{
				ans += 0;
			}
			if (dest.contains("M"))
			{
				ans += 1;
			} else
			{
				ans += 0;
			}
			return ans;
		}
		return "000";
	}

	public static String get_bin_jmp(String jmp)
	{
		if (jmp != "")
		{
			Map<String, String> map = Map.ofEntries(entry("JGT", "001"), entry("JEQ", "010"), entry("JGE", "011"),
					entry("JLT", "100"), entry("JNE", "101"), entry("JLE", "110"), entry("JMP", "111"));
			return map.get(jmp);
		}
		return "000";
	}

	public static String get_bin(int num)
	{
		return String.format("%16s", Integer.toBinaryString(num)).replace(" ", "0");
	}

	public static String get_bin_comp(String comp)
	{
		Map<String, String> map = Map.ofEntries(entry("0", "0101010"), entry("1", "0111111"), entry("-1", "0111010"),
				entry("D", "0001100"), entry("A", "0110000"), entry("!D", "0001101"), entry("!A", "0110001"),
				entry("-D", "0001111"), entry("-A", "0110011"), entry("D+1", "0011111"), entry("A+1", "0110111"),
				entry("D-1", "0001110"), entry("A-1", "0110010"), entry("D+A", "0000010"), entry("D-A", "0010011"),
				entry("A-D", "0000111"), entry("D&A", "0000000"), entry("D|A", "0010101"), entry("M", "1110000"),
				entry("!M", "1110001"), entry("-M", "1110011"), entry("M+1", "1110111"), entry("M-1", "1110010"),
				entry("D+M", "1000010"), entry("D-M", "1010011"), entry("M-D", "1000111"), entry("D&M", "1000000"),
				entry("D|M", "1010101"));
		return map.get(comp);
	}
}
