#!/usr/bin/env ruby
require 'csv'

allowed = nil
if ARGV[0] =~ /,/
	allowed = {}
	ARGV[0].split(",").each do |tag|
		allowed[tag] = 1
	end
	ARGV.shift
end

s = []
t = {}

ARGV.each do |file|
	CSV.open file do |csv|
		csv.gets # remove header
		csv.each do |id,date,by,text|
			tags = []
			while text.sub! /#(\S+)/, ""
				tags.push $1.downcase
			end
			tags.each do |tag|
				allowed and !allowed[tag] and next
				t[tag] = 1
				s.push "#{tag} #{text.sub(/^\s+/, "").sub(/\s+$/, "").gsub /\s+/, " "}"
			end
		end
	end
end

puts t.size
puts t.keys.join "\n"

s.uniq!
puts s.size
s.each do |ln|
	puts ln
end
